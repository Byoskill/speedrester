function json(value) {
    if (!value) return "undefined";
    return context.JSONL.stringify(value);
}

exports.json = json;
