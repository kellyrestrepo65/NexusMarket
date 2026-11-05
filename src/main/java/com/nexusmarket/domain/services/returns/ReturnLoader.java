package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class ReturnLoader {

    private final ReturnRepositoryPort returnRepositoryPort;

    public ReturnLoader(ReturnRepositoryPort returnRepositoryPort) {
        this.returnRepositoryPort = returnRepositoryPort;
    }

    public Return load(EntityId returnId) {
        return returnRepositoryPort.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException("Return", returnId));
    }
}
