/*******************************************************************************
 * Copyright (c) 2015 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *	 Jeff Martin - initial API and implementation
 ******************************************************************************/

package cuchaz.enigma;

import cuchaz.enigma.classprovider.ClasspathClassProvider;
import cuchaz.enigma.translation.TranslateResult;
import cuchaz.enigma.translation.Translator;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.serde.MappingFormat;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.representation.entry.Entry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static cuchaz.enigma.TestEntryFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestTranslator {
	public static final Path JAR = Path.of("build/test-obf/translation.jar");

	private static Enigma enigma;
	private static EnigmaProject project;
	private static EntryTree<EntryMapping> mappings;
	private static Translator deobfuscator;

	@BeforeAll
	public static void beforeClass() throws Exception {
		enigma = Enigma.create();
		project = enigma.openJar(JAR, new ClasspathClassProvider(), ProgressListener.none());
		mappings = MappingFormat.ENIGMA_FILE.read(
				Path.of(TestTranslator.class.getResource("/translation.mappings").toURI()),
				ProgressListener.none(), enigma.getProfile().getMappingSaveParameters());
		project.setMappings(mappings);
		deobfuscator = project.getMapper().getDeobfuscator();
	}

	@Test
	public void basicClasses() {
		assertMapping(newClass("a"), newClass("deobf/A_Basic"));
		assertMapping(newClass("b"), newClass("deobf/B_BaseClass"));
		assertMapping(newClass("c"), newClass("deobf/C_SubClass"));
	}

	@Test
	public void basicFields() {
		assertMapping(newField("a", "a", "I"), newField("deobf/A_Basic", "f1", "I"));
		assertMapping(newField("a", "a", "F"), newField("deobf/A_Basic", "f2", "F"));
		assertMapping(newField("a", "a", "Ljava/lang/String;"), newField("deobf/A_Basic", "f3", "Ljava/lang/String;"));
	}

	@Test
	public void basicMethods() {
		assertMapping(newMethod("a", "a", "()V"), newMethod("deobf/A_Basic", "m1", "()V"));
		assertMapping(newMethod("a", "a", "()I"), newMethod("deobf/A_Basic", "m2", "()I"));
		assertMapping(newMethod("a", "a", "(I)V"), newMethod("deobf/A_Basic", "m3", "(I)V"));
		assertMapping(newMethod("a", "a", "(I)I"), newMethod("deobf/A_Basic", "m4", "(I)I"));
	}

	// TODO: basic constructors

	@Test
	public void inheritanceFields() {
		assertMapping(newField("b", "a", "I"), newField("deobf/B_BaseClass", "f1", "I"));
		assertMapping(newField("b", "a", "C"), newField("deobf/B_BaseClass", "f2", "C"));
		assertMapping(newField("c", "b", "I"), newField("deobf/C_SubClass", "f3", "I"));
		assertMapping(newField("c", "c", "I"), newField("deobf/C_SubClass", "f4", "I"));
	}

	@Test
	public void inheritanceFieldsShadowing() {
		assertMapping(newField("c", "b", "C"), newField("deobf/C_SubClass", "f2", "C"));
	}

	@Test
	public void inheritanceFieldsBySubClass() {
		assertMapping(newField("c", "a", "I"), newField("deobf/C_SubClass", "f1", "I"));
		// NOTE: can't reference b.C by subclass since it's shadowed
	}

	@Test
	public void inheritanceMethods() {
		assertMapping(newMethod("b", "a", "()I"), newMethod("deobf/B_BaseClass", "m1", "()I"));
		assertMapping(newMethod("b", "b", "()I"), newMethod("deobf/B_BaseClass", "m2", "()I"));
		assertMapping(newMethod("c", "c", "()I"), newMethod("deobf/C_SubClass", "m3", "()I"));
	}

	@Test
	public void inheritanceMethodsOverrides() {
		assertMapping(newMethod("c", "a", "()I"), newMethod("deobf/C_SubClass", "m1", "()I"));
	}

	@Test
	public void inheritanceMethodsBySubClass() {
		assertMapping(newMethod("c", "b", "()I"), newMethod("deobf/C_SubClass", "m2", "()I"));
	}

	@Test
	public void innerClasses() {
		// classes
		assertMapping(newClass("g"), newClass("deobf/G_OuterClass"));
		assertMapping(newClass("g$a"), newClass("deobf/G_OuterClass$A_InnerClass"));
		assertMapping(newClass("g$a$a"), newClass("deobf/G_OuterClass$A_InnerClass$A_InnerInnerClass"));
		assertMapping(newClass("g$b"), newClass("deobf/G_OuterClass$b"));
		assertMapping(newClass("g$b$a"), newClass("deobf/G_OuterClass$b$A_NamedInnerClass"));

		// fields
		assertMapping(newField("g$a", "a", "I"), newField("deobf/G_OuterClass$A_InnerClass", "f1", "I"));
		assertMapping(newField("g$a", "a", "Ljava/lang/String;"), newField("deobf/G_OuterClass$A_InnerClass", "f2", "Ljava/lang/String;"));
		assertMapping(newField("g$a$a", "a", "I"), newField("deobf/G_OuterClass$A_InnerClass$A_InnerInnerClass", "f3", "I"));
		assertMapping(newField("g$b$a", "a", "I"), newField("deobf/G_OuterClass$b$A_NamedInnerClass", "f4", "I"));

		// methods
		assertMapping(newMethod("g$a", "a", "()V"), newMethod("deobf/G_OuterClass$A_InnerClass", "m1", "()V"));
		assertMapping(newMethod("g$a$a", "a", "()V"), newMethod("deobf/G_OuterClass$A_InnerClass$A_InnerInnerClass", "m2", "()V"));
	}

	@Test
	public void namelessClass() {
		assertMapping(newClass("h"), newClass("h"));
	}

	@Test
	public void testGenerics() {
		// classes
		assertMapping(newClass("i"), newClass("deobf/I_Generics"));
		assertMapping(newClass("i$a"), newClass("deobf/I_Generics$A_Type"));
		assertMapping(newClass("i$b"), newClass("deobf/I_Generics$B_Generic"));

		// fields
		assertMapping(newField("i", "a", "Ljava/util/List;"), newField("deobf/I_Generics", "f1", "Ljava/util/List;"));
		assertMapping(newField("i", "b", "Ljava/util/List;"), newField("deobf/I_Generics", "f2", "Ljava/util/List;"));
		assertMapping(newField("i", "a", "Ljava/util/Map;"), newField("deobf/I_Generics", "f3", "Ljava/util/Map;"));
		assertMapping(newField("i$b", "a", "Ljava/lang/Object;"), newField("deobf/I_Generics$B_Generic", "f4", "Ljava/lang/Object;"));
		assertMapping(newField("i", "a", "Li$b;"), newField("deobf/I_Generics", "f5", "Ldeobf/I_Generics$B_Generic;"));
		assertMapping(newField("i", "b", "Li$b;"), newField("deobf/I_Generics", "f6", "Ldeobf/I_Generics$B_Generic;"));

		// methods
		assertMapping(newMethod("i$b", "a", "()Ljava/lang/Object;"), newMethod("deobf/I_Generics$B_Generic", "m1", "()Ljava/lang/Object;"));
	}

	private void assertMapping(Entry<?> obf, Entry<?> deobf) {
		TranslateResult<? extends Entry<?>> result = deobfuscator.extendedTranslate(obf);
		assertThat(result, is(notNullValue()));
		assertThat(result.getValue(), is(deobf));

		String deobfName = result.getValue().getName();
		if (deobfName != null) {
			assertThat(deobfName, is(deobf.getName()));
		}
	}
}
