const Client = require('../../lib/rest-client').Client;
const EndpointConfig = require('../../lib/rest-client').EndpointConfig;
const chai = require('chai');
const assert = chai.assert; // Using Assert style

// Entities
const defaultParams = require('../../entities/default-params.json');

//Optional const response = require('../../entities/article-controller/getAllComboBoxArticlegetAllComboBoxArticle_response.json');

const client = new Client();
describe('{{{controller}}}', () => {
    /**
     * Test of the endpoint {{{method}}} {{{url}}}
     * for the controller {{{controller}}}
     */
    it('{{{operationId}}}::{{{method}}}', async () => {
        const endpointConfig = new EndpointConfig();
        endpointConfig.method = {{{literal method}}};

        endpointConfig.pathParams = {
            {{#each pathParams}} {{{@key}}}: {{{this}}} {{/each}}

        };
        endpointConfig.url = {{{literal url}}};
        endpointConfig.produces = {{{literal produces}}};
        endpointConfig.consumes = {{{literal consumes}}};

        return client
            .execute(endpointConfig)
            .then((response) => {
                response.hasStatus(200);
                {{#if hasBody}}
                response.hasBody("{{{controller}}}/{{{operationId}}}-response.json", {{{isJson}}});
                {{/if}}
            })
            .catch((err) => {
                console.log(err);
                assert.fail('REST Call has failed for the reason : ' + err);
            });
    });
});
