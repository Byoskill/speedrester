function getBaseType(type) {

	if (type.variant == "class") {
        if (!type.canonicalName.startsWith("java.lang") && !type.primitive) {
            globals.usedJavaTypes.add(type.canonicalName);
        }
		return type.simpleName;
	} else if (type.variant == "parameterized") {
		var tpm = type.typeParameters.map(t => getBaseType(t)).join(',');		
		return getBaseType(type.rawtype) + "<" + tpm + ">";
	}
}

function javaType(type) {
    const baseType = getBaseType(type);
    return type.array ? baseType + "[]": baseType;
}

exports.javaType = javaType;