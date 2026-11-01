package br.com.vaultkeeper.magazine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConditionConverterTest {

    final ConditionConverter conversor = new ConditionConverter();

    @Test
    void ida_e_volta_para_todas_as_condicoes() {
        for (var c : Condition.values()) {
            assertThat(conversor.convertToEntityAttribute(conversor.convertToDatabaseColumn(c))).isEqualTo(c);
        }
        assertThat(conversor.convertToDatabaseColumn(Condition.EXCELENTE)).isEqualTo("Excelente");
    }

    @Test
    void nulo_continua_nulo() {
        assertThat(conversor.convertToDatabaseColumn(null)).isNull();
        assertThat(conversor.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void texto_desconhecido_no_banco_falha_com_mensagem_clara() {
        assertThatThrownBy(() -> conversor.convertToEntityAttribute("Péssimo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Péssimo");
    }
}
