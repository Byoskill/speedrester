const logger = require('./logger').logger;
const ServerConfiguration = require('./config');

const serverConfig = new ServerConfiguration();

/**
 * Defines the params provided by the Test.
 */
class EndpointConfig {
    constructor() {
        this.pathParams = {};
        this.queryParams = {};
        this.url = '';
        this.computedUrl = '';
        this.headers = {};
        this.method = 'get';
    }

    buildUrl() {
        // Escape URL
        this.computedUrl = this.url;
        for (const paramKey in this.pathParams) {
            const escapedParameter = encodeURIComponent(this.pathParams[paramKey]);
            // URI params are escaped.
            this.computedUrl = this.computedUrl.replace(`{${paramKey}}`, escapedParameter);
        }

        if (this.url != this.computedUrl) {
            logger.info(`Computed URL is ${this.computedUrl}`);
        }
        return this.computedUrl;
    }

    build() {
        return {
            method: this.method,
            url: this.buildUrl(),
            params: this.queryParams,
            headers: { ...serverConfig.headers(), ...this.headers }
        };
    }
}



module.exports = EndpointConfig;