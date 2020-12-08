function literal(constant) {
    if (!constant) return "null";
    return '"' + constant.replace(/(['"])/g, '\\$1') + '"';
}

exports.literal = literal;
