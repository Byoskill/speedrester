/**
 * Byoskill Leroy - All rights reserved 2020-2021
 */
const util = require('util');
const axios = require('axios').default;
const fs = require('fs');
const yaml = require('js-yaml');

fs.readFileAsync = util.promisify(fs.readFile);

/**
  * This component is in charge to load a swagger/OpenAPI definition from an REST API.
  */
class SwaggerLoader {

    /**
     * Provides the type yml|json
     */
    constructor(expectedType) {
        this.expectedType = expectedType;

    }

    /**
     * Loads a swagger from an URL and returns a promise with the swagger info.
     * @param {url} the url to the swagger
     */
    loadFromUrl(url) {
        
        return axios.get(url)
            .then(response => this.handleUrlResponse(response));


    }

    loadFromFile(file) {

        return fs.readFileAsync(file)
            .then(response => this.handleFileResource(response));
    }

    /**
     * Detects YAML based on the extension only
     */
    isYmlResource() {
        return true;
    }

    handleUrlResponse(response) {
        if (this.expectedType == 'json') {
            return response.data;
        }
        return response.data;

    }


    handleFileResource(response) {
        if (this.expectedType == 'json') {
            return JSON.parse(response + '');
        }
        return response.data;

    }

}

module.exports.SwaggerLoader = SwaggerLoader;