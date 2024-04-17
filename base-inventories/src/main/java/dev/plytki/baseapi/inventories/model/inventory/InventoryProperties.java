package dev.plytki.baseapi.inventories.model.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InventoryProperties {

    private String name;
    private int lines;
    private CancelPolicy cancelPolicy;
    private CancelPolicy cancelPolicyPlayer;

    public InventoryProperties(String name, int lines) {
        this.name = name;
        this.lines = lines;
    }

    public String name() {
        return name;
    }

    public InventoryProperties setName(String name) {
        this.name = name;
        return this;
    }

    public int lines() {
        return lines;
    }

    public InventoryProperties setLines(int lines) {
        this.lines = lines;
        return this;
    }

    public CancelPolicy cancelPolicy() {
        return cancelPolicy;
    }

    public InventoryProperties setCancelPolicy(CancelPolicy cancelPolicy) {
        this.cancelPolicy = cancelPolicy;
        return this;
    }

    public CancelPolicy cancelPolicyPlayer() {
        return cancelPolicyPlayer;
    }

    public InventoryProperties setCancelPolicyPlayer(CancelPolicy cancelPolicyPlayer) {
        this.cancelPolicyPlayer = cancelPolicyPlayer;
        return this;
    }
}
