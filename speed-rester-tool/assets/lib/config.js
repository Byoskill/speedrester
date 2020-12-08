/**
 * Here you may define the configuration used by the Axios rest client
 */
require('dotenv').config();
const AbstractServerConfiguration = require('./config-utils');

class ServerConfiguration extends AbstractServerConfiguration {
	timeout() {
		return process.env.TEST_CLIENT_TIMEOUT || 1000;
	}

	getBaseUrl() {
		return process.env.TEST_CLIENT_BASE_URL;
	}

	headers() {
		const headerMap = this.readHeaders();

		return {
			...headerMap,
			"X-Example": 'EXAMPLE'
		};
	}

	auth() {
		return {
			// See axios documentation https://github.com/axios/axios
			// Return undefined if you don't need it.
			auth: {}
		};
	}

	// `responseType` indicates the type of data that the server will respond with
	// options are: 'arraybuffer', 'document', 'json', 'text', 'stream'
	//   browser only: 'blob'
	responseType() {
		return 'json';
	}

	// `responseEncoding` indicates encoding to use for decoding responses (Node.js only)
	// Note: Ignored for `responseType` of 'stream' or client-side requests
	responseEncoding() {
		return 'utf8';
	}

	maxRedirects() {
		return 5;
	}

	proxy() {
		/**return {
			protocol: 'https',
			host: '127.0.0.1',
			port: 9000,
			auth: {
				username: 'mikeymike',
				password: 'rapunz3l'
			}
        };
        */
       return undefined;
	}
}

module.exports = ServerConfiguration;
