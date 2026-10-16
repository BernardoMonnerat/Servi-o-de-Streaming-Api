package br.com.api.streaming_plataform.Enum;

// A ordem importa: quem esta mais abaixo pode ver o conteudo de quem esta mais acima
// (ADULTO ve tudo, ADOLESCENTE ve INFANTIL e ADOLESCENTE, INFANTIL ve so INFANTIL)
public enum Perfil {
    INFANTIL, ADOLESCENTE, ADULTO
}
