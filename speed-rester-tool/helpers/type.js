
function type(qualifiedName) {
	const split = qualifiedName.split(".");
	globals.usedJavaTypes.add(qualifiedName);
    return split[split.length-1];
}

exports.type = type;