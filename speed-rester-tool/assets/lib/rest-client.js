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
        logger.error(`axios::error ${error}`);
        //console.log(error);
        return new TestResponseWrapper(error);
    }
}

module.exports.Client = Client;
module.exports.EndpointConfig = EndpointConfig;
