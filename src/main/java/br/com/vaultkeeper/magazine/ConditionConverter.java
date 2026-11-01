package br.com.vaultkeeper.magazine;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Traduz entre o enum Java e o texto da coluna `condition` ('Excelente', 'Bom', ...).
// null continua null nos dois sentidos: "sem condição informada" é um valor válido.
@Converter
public class ConditionConverter implements AttributeConverter<Condition, String> {

    @Override
    public String convertToDatabaseColumn(Condition condition) {
        return condition == null ? null : condition.label();
    }

    @Override
    public Condition convertToEntityAttribute(String label) {
        return label == null ? null : Condition.fromLabel(label);
    }
}
