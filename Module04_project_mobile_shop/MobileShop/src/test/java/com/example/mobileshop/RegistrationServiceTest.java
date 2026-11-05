package com.example.mobileshop;
import com.example.mobileshop.dto.RegisterRequest;
import com.example.mobileshop.entity.AdminAccount;
import com.example.mobileshop.repository.AdminAccountRepository;
import com.example.mobileshop.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
 @Mock AdminAccountRepository accounts;
 PasswordEncoder encoder=new BCryptPasswordEncoder();
 RegisterRequest valid(){RegisterRequest r=new RegisterRequest();r.setUsername("staff_1");r.setPassword("Admin@123");r.setConfirmPassword("Admin@123");return r;}
 @Test void passwordIsHashedBeforeSaving(){
    var service=new RegistrationService(accounts,encoder);
    when(accounts.existsByUsername("staff_1")).thenReturn(false);
    when(accounts.saveAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));
    service.register(valid());
    var cap=org.mockito.ArgumentCaptor.forClass(AdminAccount.class);
    verify(accounts).saveAndFlush(cap.capture());
    assertNotEquals("Admin@123",cap.getValue().getPasswordHash());
    assertTrue(encoder.matches("Admin@123",cap.getValue().getPasswordHash()));
 }
 @Test void rejectDuplicateUsername(){
    when(accounts.existsByUsername("staff_1")).thenReturn(true);
    var service=new RegistrationService(accounts,encoder);
    assertThrows(IllegalArgumentException.class,()->service.register(valid()));
    verify(accounts,never()).saveAndFlush(any());
 }
 @Test void rejectMismatchedPassword(){
    var request=valid();request.setConfirmPassword("NotTheSame@12");
    var service=new RegistrationService(accounts,encoder);
    assertThrows(IllegalArgumentException.class,()->service.register(request));
    verifyNoInteractions(accounts);
 }
}
