package com.asg.security.gateway.service;

import com.asg.security.gateway.repository.HRPayRollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HRPayRollService{

    private final HRPayRollRepository hrPayRollRepository;

    public String SyncHRDataAction() {
        return hrPayRollRepository.SyncHRDataSP();
    }
}
