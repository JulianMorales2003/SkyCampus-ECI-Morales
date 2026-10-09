package v2.model;

public enum TipoDrone {
    MINI(1, 1000),
    CARGO(100, 2000),
    EXPRESS(1, 1000);

    private final int pesoMinimoGramos;
    private final int capacidadGramos;

    TipoDrone(int pesoMinimoGramos, int capacidadGramos) {
        this.pesoMinimoGramos = pesoMinimoGramos;
        this.capacidadGramos = capacidadGramos;
    }

    public int pesoMinimoGramos() {
        return pesoMinimoGramos;
    }

    public int capacidadGramos() {
        return capacidadGramos;
    }

    /** RN-02 y RN-04: el peso debe estar entre el mínimo del tipo y su capacidad (ambos incluidos). */
    public boolean admitePeso(int gramos) {
        return gramos >= pesoMinimoGramos && gramos <= capacidadGramos;
    }
}
