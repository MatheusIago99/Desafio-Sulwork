package br.com.sulwork.alergeno;

/**
 * Lista fechada de alérgenos, usada tanto nas restrições dos participantes quanto
 * nos itens. Ter o mesmo vocabulário dos dois lados é o que permite cruzar as
 * informações e gerar alertas. Restrições que não são alérgenos (ex.: vegetariano)
 * ficam no campo de observações em texto livre do participante.
 */
public enum Alergeno {
    GLUTEN("Glúten"),
    LACTOSE("Lactose"),
    OVO("Ovo"),
    AMENDOIM("Amendoim"),
    CASTANHAS("Castanhas e nozes"),
    SOJA("Soja"),
    PEIXE("Peixe"),
    CRUSTACEOS("Crustáceos e frutos do mar"),
    GERGELIM("Gergelim");

    private final String descricao;

    Alergeno(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
