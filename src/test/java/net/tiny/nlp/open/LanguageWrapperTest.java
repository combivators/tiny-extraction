package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LanguageWrapperTest {


    @Test
    public void testGetter() throws Exception {
        LanguageWrapper wrapper = new LanguageWrapper();
        String[] codes = wrapper.getCodes();
        assertEquals(103, codes.length);
        assertEquals("Mandarin Chinese", wrapper.getLanguage("cmn"));
        assertEquals("Japanese", wrapper.getLanguage("jpn"));
        assertEquals("English", wrapper.getLanguage("eng"));
    }

    @Test
    public void testGuestLanguageWrapper() throws Exception {
        LanguageWrapper wrapper = new LanguageWrapper();
        // Get the most probable language
        assertEquals("English", wrapper.guest("English Japanese Mandarin Chinese"));
        assertEquals("Japanese", wrapper.guest("お名前"));
        assertEquals("Mandarin Chinese", wrapper.guest("名前")); //??
        assertEquals("Mandarin Chinese", wrapper.guest("姓名"));
        assertEquals("Spanish", wrapper.guest("A la fecha tres calles bonaerenses recuerdan su nombre (en Ituzaingó, Merlo y Campana)."));
        assertEquals("Standard Latvian", wrapper.guest("Egija Tri-Active procedūru īpaši iesaka izmantot siltākajos gadalaikos"));
        assertEquals("Serbian", wrapper.guest("Већина становника боравила је кућама од блата или шаторима"));
        assertEquals("German", wrapper.guest("Alle Jahre wieder: Millionen Spanier haben am Dienstag die Auslosung in der größten Lotterie der Welt verfolgt"));
    }

}
