package com.felipegiotto.utils.config;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class FGPropertiesTest {

	private static final String CHAVE = "chave";
	private static File propertiesFile = new File("tmp/test.properties");
	
	@BeforeClass
	public static void prepararPasta() {
		propertiesFile.getParentFile().mkdirs();
		propertiesFile.delete();
	}
	
	@After
	public void excluir() {
		propertiesFile.delete();
	}
	
	@Test
	public void setInt() throws IOException {
		FGProperties p = new FGProperties(propertiesFile.toPath(), false);
		assertEquals(null, p.getString(CHAVE));
		assertEquals(null, p.getInt(CHAVE));
		assertFalse(p.containsKey(CHAVE));
		
		p.setInt(CHAVE, 10);
		assertTrue(p.containsKey(CHAVE));
		assertEquals("10", p.getString(CHAVE));
		assertEquals(10, p.getInt(CHAVE).intValue());
		
		p.setInt(CHAVE, null);
		assertTrue(p.containsKey(CHAVE));
		assertEquals(null, p.getString(CHAVE));
		assertEquals(null, p.getInt(CHAVE));
		
		p.remove(CHAVE);
		assertEquals(null, p.getString(CHAVE));
		assertEquals(null, p.getInt(CHAVE));
		assertFalse(p.containsKey(CHAVE));
		
		// Testa gravação em arquivo
		p.setInt(CHAVE, 11);
		p.save();
		p = new FGProperties(propertiesFile.toPath(), false);
		assertTrue(p.containsKey(CHAVE));
		assertEquals("11", p.getString(CHAVE));
		assertEquals(11, p.getInt(CHAVE).intValue());
		
		// Testa atributo "changed"
		assertEquals(false, p.isChanged());
		p.setInt(CHAVE, 12);
		assertEquals(true, p.isChanged());
		p.saveIfModified();
		p.setInt(CHAVE, 12);
		assertEquals(false, p.isChanged());
	}
	
	@Test
	public void setDouble() throws IOException {
		FGProperties p = new FGProperties(propertiesFile.toPath(), false);
		assertEquals(null, p.getString(CHAVE));
		assertEquals(null, p.getDouble(CHAVE));
		assertFalse(p.containsKey(CHAVE));
		
		p.setDouble(CHAVE, 10.5);
		assertTrue(p.containsKey(CHAVE));
		assertEquals("10.5", p.getString(CHAVE));
		assertEquals(10.5, p.getDouble(CHAVE).doubleValue(), 0.001);
		
		p.setDouble(CHAVE, null);
		assertTrue(p.containsKey(CHAVE));
		assertEquals(null, p.getString(CHAVE));
		assertEquals(null, p.getDouble(CHAVE));
		
		p.remove(CHAVE);
		assertEquals(null, p.getString(CHAVE));
		assertEquals(null, p.getDouble(CHAVE));
		assertFalse(p.containsKey(CHAVE));
		
		// Testa gravação em arquivo
		p.setDouble(CHAVE, 11.5);
		p.save();
		p = new FGProperties(propertiesFile.toPath(), false);
		assertTrue(p.containsKey(CHAVE));
		assertEquals("11.5", p.getString(CHAVE));
		assertEquals(11.5, p.getDouble(CHAVE).doubleValue(), 0.001);
		
		// Testa atributo "changed"
		assertEquals(false, p.isChanged());
		p.setDouble(CHAVE, 12.5);
		assertEquals(true, p.isChanged());
		p.saveIfModified();
		p.setDouble(CHAVE, 12.5);
		assertEquals(false, p.isChanged());
	}
	
	@Test
	public void setString() throws IOException {
		FGProperties p = new FGProperties(propertiesFile.toPath(), false);
		assertEquals(null, p.getString(CHAVE));
		assertFalse(p.containsKey(CHAVE));
		
		p.setString(CHAVE, "valor");
		assertTrue(p.containsKey(CHAVE));
		assertEquals("valor", p.getString(CHAVE));
		
		p.setString(CHAVE, null);
		assertTrue(p.containsKey(CHAVE));
		assertEquals(null, p.getString(CHAVE));
		
		p.remove(CHAVE);
		assertEquals(null, p.getString(CHAVE));
		assertFalse(p.containsKey(CHAVE));
		
		// Testa gravação em arquivo
		p.setString(CHAVE, "valor");
		p.save();
		p = new FGProperties(propertiesFile.toPath(), false);
		assertTrue(p.containsKey(CHAVE));
		assertEquals("valor", p.getString(CHAVE));
		
		// Testa atributo "changed"
		assertEquals(false, p.isChanged());
		p.setString(CHAVE, "valor 2");
		assertEquals(true, p.isChanged());
		p.saveIfModified();
		p.setString(CHAVE, "valor 2");
		assertEquals(false, p.isChanged());
	}
	
	@Test
	public void setStringMultiLineAccent() throws IOException {
		FGProperties p = new FGProperties(propertiesFile.toPath(), false);
		
		p.setString("C1", "L1\n\nL2ÁÇ\n<br />");
		p.setString("C2", "L1\\n\\nL2ÁÇ\\n<br />");
		assertTrue(p.containsKey("C1"));
		assertEquals("L1\n\nL2ÁÇ\n<br />", p.getString("C1"));
		assertEquals("L1\\n\\nL2ÁÇ\\n<br />", p.getString("C2"));
		
		// Testa gravação em arquivo
		p.save();
		p = new FGProperties(propertiesFile.toPath(), false);
		assertTrue(p.containsKey("C1"));
		assertEquals("L1\n\nL2ÁÇ\n<br />", p.getString("C1"));
		assertEquals("L1\\n\\nL2ÁÇ\\n<br />", p.getString("C2"));
	}
	
	@Test
	public void remove() throws IOException {
		FGProperties p = new FGProperties(propertiesFile.toPath(), false);
		assertFalse(p.containsKey(CHAVE));
		
		p.setString(CHAVE, "valor");
		assertTrue(p.isChanged());
		p.saveIfModified();
		assertFalse(p.isChanged());
		
		p.remove(CHAVE);
		assertTrue(p.isChanged());
		p.saveIfModified();
		assertFalse(p.isChanged());
		
		p.remove(CHAVE);
		assertFalse(p.isChanged());
	}
}
