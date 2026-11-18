package com.graund.block.tile;

public enum FaceMode {
    OPAQUE("Opaque"),
    MASK("Mask"),
    ONE_WAY("One-way window"),
    GLASS("Glass");

    private final String displayName;

    FaceMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FaceMode next() {
        FaceMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}