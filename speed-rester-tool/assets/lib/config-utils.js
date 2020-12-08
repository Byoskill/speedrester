/**
 * Defines utility methods for the Server configuration class
 */
class AbstractServerConfiguration {

    readHeaders() {
        // Header pairs are separated by ;
        const headers = process.env.TEST_CLIENT_DEFAULT_HEADERS.split(";");

        const headerMap = {};

        for( let header of headers) {
            const headerPair = header.split("=");
            const headerValue = headerPair[0];
            const headerKey = headerPair[1]; 
            headerMap[headerKey] = headerValue;
        }
        return headerMap;
    }
}

module.exports = AbstractServerConfiguration;