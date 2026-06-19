package com.example.inventory.controller;

import com.example.inventory.entity.Inventory;
import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public Inventory reserveInventory(
            @RequestBody Inventory inventory) {

        return inventoryService
                .reserveInventory(inventory);
    }

    @PutMapping("/release/{id}")
    public Inventory releaseInventory(
            @PathVariable Long id) {

        return inventoryService
                .releaseInventory(id);
    }

    @GetMapping
    public List<Inventory> getAllInventory() {

        return inventoryService.getAllInventory();
    }
}