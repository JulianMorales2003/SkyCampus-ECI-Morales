package model;

public enum TipoCarga {
    SOBRE(50),
    CARPETA(300),
    LIBRO(800);

    private final int pesoGramos;

    TipoCarga(int pesoGramos) {
        this.pesoGramos = pesoGramos;
    }

    public int pesoGramos() {
        return pesoGramos;
    }
}
