package cuchaz.enigma;

import cuchaz.enigma.config.ConfigContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {
	@Test
	public void serialize() {
		ConfigContainer cc = new ConfigContainer();
		cc.data().setString("a", "a");
		cc.data().section("a").section("b").section("c").setString("a", "abcd");
		cc.data().section("a").section("b").section("c").setBool("b", true);
		cc.data().section("a").section("b").section("c").setInt("c", 5);
		cc.data().section("a").section("b").section("c").setDouble("d", 3.5);
		cc.data().section("a").section("b").section("c").setRgbColor("e", 0x123456);
		assertEquals("""
				a=a

				[a][b][c]
				a=abcd
				b=true
				c=5
				d=3.5
				e=#123456
				""", cc.serialize());
	}

	@Test
	public void deserialize() {
		ConfigContainer cc = new ConfigContainer();
		cc.data().setString("a", "a");
		cc.data().section("a").section("b").section("c").setString("a", "abcd");
		cc.data().section("a").section("b").section("c").setBool("b", true);
		cc.data().section("a").section("b").section("c").setInt("c", 5);
		cc.data().section("a").section("b").section("c").setDouble("d", 3.5);
		cc.data().section("a").section("b").section("c").setRgbColor("e", 0x123456);
		assertEquals(ConfigContainer.parse("""
				a=a

				[a][b][c]
				a=abcd
				b=true
				c=5
				d=3.5
				e=#123456
				""").data(), cc.data());
	}

	@Test
	public void weirdChars() {
		ConfigContainer cc = new ConfigContainer();
		String thing = "\\[],\\,./'\"`~!@#$%^&*()_+-=|}{\n\\\\\r\b\u0000\uffff\u1234";
		cc.data().section(thing).setString(thing, thing);
		cc.data().section(thing).setArray("arr", new String[] { thing, thing, thing, thing });

		assertEquals(
				"""
						[\\\\[\\],\\\\,./'"`~!@#$%^&*()_+-=|}{\\n\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234]
						\\\\\\[],\\\\,./'"`~!@#$%^&*()_+-\\=|}{\\n\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234=\\\\[],\\\\,./'"`~!@#$%^&*()_+-=|}{\\n\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234
						arr=\\\\\\\\[]\\\\,\\\\\\\\\\\\,./'"`~!@#$%^&*()_+-=|}{\\n\\\\\\\\\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234,\\\\\\\\[]\\\\,\\\\\\\\\\\\,./'"`~!@#$%^&*()_+-=|}{\\n\\\\\\\\\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234,\\\\\\\\[]\\\\,\\\\\\\\\\\\,./'"`~!@#$%^&*()_+-=|}{\\n\\\\\\\\\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234,\\\\\\\\[]\\\\,\\\\\\\\\\\\,./'"`~!@#$%^&*()_+-=|}{\\n\\\\\\\\\\\\\\\\\\u000d\\u0008\\u0000\\uffff\\u1234
						""",
				cc.serialize());

		ConfigContainer cc1 = ConfigContainer.parse(cc.serialize());
		assertEquals(cc.data(), cc1.data());

		cc1 = ConfigContainer.parse(cc1.serialize());
		assertEquals(cc.data(), cc1.data());
	}

	@Test
	public void syntaxErrors() {
		assertEquals("", ConfigContainer.parse("abcde").serialize());
		assertEquals("", ConfigContainer.parse("what\\=?").serialize());

		assertEquals("[a]\nb=c\n", ConfigContainer.parse("[a] what is this\nb=c").serialize());
		assertEquals("b=c\n", ConfigContainer.parse("[a][ what is this\nb=c").serialize());
		assertEquals("", ConfigContainer.parse("[").serialize());
		assertEquals("[a]\na=b\nc=d\n", ConfigContainer.parse("[a]\na=b\n[\nc=d").serialize());


		// not technically syntax errors but never something that gets generated
		assertEquals("", ConfigContainer.parse("[a]").serialize());
		assertEquals("", ConfigContainer.parse("[a]\n[b]").serialize());
	}
}
