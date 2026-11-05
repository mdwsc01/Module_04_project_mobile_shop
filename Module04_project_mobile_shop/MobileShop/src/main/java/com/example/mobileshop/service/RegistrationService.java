package com.example.mobileshop.service;
import com.example.mobileshop.dto.RegisterRequest;
import com.example.mobileshop.entity.AdminAccount;
import com.example.mobileshop.repository.AdminAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final AdminAccountRepository accounts;
    private final PasswordEncoder encoder;
    public RegistrationService(AdminAccountRepository accounts, PasswordEncoder encoder){
        this.accounts=accounts;this.encoder=encoder;
    }
    @Transactional
    public void register(RegisterRequest request) {
        String name=request.getUsername().trim();
        if(!request.getPassword().equals(request.getConfirmPassword()))
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp");
        if(accounts.existsByUsername(name)) throw new IllegalArgumentException("Username đã tồn tại");
        AdminAccount account=new AdminAccount();account.setUsername(name);
        account.setPasswordHash(encoder.encode(request.getPassword()));
        accounts.saveAndFlush(account);
    }
}
