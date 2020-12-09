const axios = require('axios').default;
const logger = require('./logger').logger;
const ServerConfiguration = require('./config');
const TestResponseWrapper = require('./rest-response-wrapper');
const EndpointConfig = require('./endpoint-config');

const serverConfig = new ServerConfiguration();

/**
 * Wrapper over Axios
 */
class Client {
    constructor() {
        this.instance = axios.create({
            baseURL: serverConfig.getBaseUrl(),
            timeout: serverConfig.timeout(),
            headers: serverConfig.headers(),
            maxRedirects: serverConfig.maxRedirects(),
            responseType: serverConfig.responseType(),
            responseEncoding: serverConfig.responseEncoding(),
            proxy: serverConfig.proxy(),
            auth: serverConfig.auth()
        });
        logger.debug(`AXIOS client initialized for the server URL ${serverConfig.getBaseUrl()}`);
    }

    execute(endpointConfig) {
        return this.instance(endpointConfig.build())
            .then((response) => this.handleResponse(response))
            .catch((err) => this.handleError(err));
    }

    handleResponse(response) {
        logger.debug(`axios::response ${JSON.stringify(response.data)}`);
        return new TestResponseWrapper(response);
    }

    handleError(error) {
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx

            logger.error(`axios::error::data ${JSON.stringify(error.response.data, null, 2)}`);
            logger.error(`axios::error::status ${JSON.stringify(error.response.status, null, 2)}`);
            logger.error(`axios::error::headers ${JSON.stringify(error.response.headers, null, 2)}`);
        } else if (error.request) {
            // The request was made but no response was received
            // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
            // http.ClientRequest in node.js
            logger.error(`axios::error::request ${JSON.stringify(error.request, null, 2)}`);
        } else {
            // Something happened in setting up the request that triggered an Error
            logger.error(`axios::error::message ${JSON.stringify(error.message, null, 2)}`);
        }
        console.log(error.config);

        return new TestResponseWrapper(error);
    }
}

module.exports.Client = Client;
module.exports.EndpointConfig = EndpointConfig;