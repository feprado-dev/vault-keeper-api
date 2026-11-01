package br.com.vaultkeeper.magazine;

// Equivalente ao `type Condition` do TypeScript, mas aqui o compilador
// garante que só estes quatro valores existem.
// O banco do Supabase guarda o texto como o React usa ('Excelente', 'Bom'...): `label`.
public enum Condition {
    EXCELENTE("Excelente"), BOM("Bom"), REGULAR("Regular"), RUIM("Ruim");

    private final String label;

    Condition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Condition fromLabel(String label) {
        for (var c : values()) {
            if (c.label.equals(label)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Condição desconhecida no banco: " + label);
    }
}
