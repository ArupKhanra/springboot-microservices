package com.example.inventory.service;

import com.example.inventory.entity.Inventory;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

//    public Inventory reserveInventory(
//            Inventory inventory) {
//
//        inventory.setStatus("RESERVED");
//
//        return inventoryRepository.save(inventory);
//    }
public Inventory reserveInventory(
        Inventory inventory) {

    boolean inventoryAvailable = false;

    if (!inventoryAvailable) {

        throw new RuntimeException(
                "Inventory Not Available");
    }

    inventory.setStatus("RESERVED");

    return inventoryRepository.save(inventory);
}

    public Inventory releaseInventory(Long id) {

        Inventory inventory =
                inventoryRepository.findById(id)
                        .orElseThrow();

        inventory.setStatus("RELEASED");

        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getAllInventory() {

        return inventoryRepository.findAll();
    }
}