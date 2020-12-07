"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const winston = require("winston");
exports.logger = winston.createLogger({
    transports: [
        new winston.transports.File({
            filename: 'error.log',
            level: 'error',
            format: winston.format.json()
        }),
        new winston.transports.File({ filename: 'service.log' })
    ]
});
//
// If we're not in production then log to the `console` with the format:
// `${info.level}: ${info.message} JSON.stringify({ ...rest }) `
//
if (process.env.NODE_ENV !== 'production') {
    exports.logger.add(new winston.transports.Console({
        level: 'info',
        format: winston.format.combine(winston.format.splat(), winston.format.colorize(), winston.format.simple())
    }));
}
else {
    exports.logger.add(new winston.transports.Console({
        level: 'info'
    }));
}
exports.logger.info('Logger is configured');
//# sourceMappingURL=logger.js.map