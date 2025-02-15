/*******************************************************************************
 * Copyright (c) 2015 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Jeff Martin - initial API and implementation
 ******************************************************************************/

package cuchaz.enigma.translation.representation.entry;

import com.google.common.base.Preconditions;

import cuchaz.enigma.source.RenamableTokenType;
import cuchaz.enigma.translation.TranslateResult;
import cuchaz.enigma.translation.Translator;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.Signature;
import cuchaz.enigma.translation.representation.TypeDescriptor;

import javax.annotation.Nonnull;

public class FieldDefEntry extends FieldEntry implements DefEntry<ClassEntry> {
	private final AccessFlags access;
	private final Signature signature;

	public FieldDefEntry(ClassEntry owner, String name, TypeDescriptor desc, Signature signature, AccessFlags access) {
		this(owner, name, desc, signature, access, null);
	}

	public FieldDefEntry(ClassEntry owner, String name, TypeDescriptor desc, Signature signature, AccessFlags access, String javadocs) {
		super(owner, name, desc, javadocs);
		Preconditions.checkNotNull(access, "Field access cannot be null");
		Preconditions.checkNotNull(signature, "Field signature cannot be null");
		this.access = access;
		this.signature = signature;
	}

	public static FieldDefEntry parse(ClassEntry owner, int access, String name, String desc, String signature) {
		return new FieldDefEntry(owner, name, new TypeDescriptor(desc), Signature.createTypedSignature(signature), new AccessFlags(access), null);
	}

	@Override
	public AccessFlags getAccess() {
		return this.access;
	}

	public Signature getSignature() {
		return this.signature;
	}

	@Override
	protected TranslateResult<FieldEntry> extendedTranslate(Translator translator, @Nonnull EntryMapping mapping) {
		TypeDescriptor translatedDesc = translator.translate(this.desc);
		Signature translatedSignature = translator.translate(this.signature);
		String translatedName = mapping.targetName() != null ? mapping.targetName() : this.name;
		AccessFlags translatedAccess = mapping.accessModifier().transform(this.access);
		String docs = mapping.javadoc();
		return TranslateResult.of(
				mapping.targetName() == null ? RenamableTokenType.OBFUSCATED : RenamableTokenType.DEOBFUSCATED,
				new FieldDefEntry(this.parent, translatedName, translatedDesc, translatedSignature, translatedAccess, docs)
		);
	}


	@Override
	public FieldDefEntry withName(String name) {
		return new FieldDefEntry(this.parent, name, this.desc, this.signature, this.access, this.javadocs);
	}

	@Override
	public FieldDefEntry withParent(ClassEntry owner) {
		return new FieldDefEntry(owner, this.name, this.desc, this.signature, this.access, this.javadocs);
	}
}
