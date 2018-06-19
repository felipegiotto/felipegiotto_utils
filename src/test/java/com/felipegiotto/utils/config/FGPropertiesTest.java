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
	}
	
	@Test
	public void setStringMultiLine() throws IOException {
		FGProperties p = new FGProperties(propertiesFile.toPath(), false);
		
		p.setString("C1", "L1\n\nL2\n<br />");
		p.setString("C2", "L1\\n\\nL2\\n<br />");
		assertTrue(p.containsKey("C1"));
		assertEquals("L1\n\nL2\n<br />", p.getString("C1"));
		assertEquals("L1\\n\\nL2\\n<br />", p.getString("C2"));
		
		// Testa gravação em arquivo
		p.save();
		p = new FGProperties(propertiesFile.toPath(), false);
		assertTrue(p.containsKey("C1"));
		assertEquals("L1\n\nL2\n<br />", p.getString("C1"));
		assertEquals("L1\\n\\nL2\\n<br />", p.getString("C2"));
	}
	
}
