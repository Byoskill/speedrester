var chai = require('chai');
var like = require('chai-like');
chai.use(require('chai-json-pattern').default);
chai.use(require('chai-json'));
chai.use(like);
//
chai.should();

var regexPlugin = like.extend({
	match: function(object, expected) {
		return typeof object === 'string' && expected instanceof RegExp;
	},
	assert: function(object, expected) {
		return expected.test(object);
	}
});

const assert = chai.assert; // Using Assert style
const expect = chai.expect; // Using Expect style
var ja = require('json-assert');

/**
 * Wrapper over the 
 */
class TestResponseWrapper {
	constructor(response) {
		this.response = response;
	}

	hasStatus(statusCode) {
		assert(this.response.status === statusCode, `Status is not ${statusCode}.`);
		return this;
	}
	hasObjectBody() {
		assert.isObject(this.response.data, 'Body should return an object.');
		return this;
	}

	/**
     * Tests using JSON assert a JSON response.
     * https://www.npmjs.com/package/json-assert
     * @param {payload} payload the expected JSON response.
     */
	jsonAssert(payload) {
		return ja.isEqual(payload, this.response.data);
	}

    // Json Like : https://www.chaijs.com/plugins/chai-like/
	jsonLike(payload) {
		this.response.data.should.like(payload);
    }
    
    // Watch https://www.chaijs.com/plugins/chai-json-pattern/
	jsonPattern(pattern) {
		expect(this.response.data).to.matchPattern(pattern);
	}

	/**
     * Tests using JSON assert a JSON response.
     * @param {payload} payload the expected JSON response.
     */
	hasJsonDeepEqual(payload) {
		return assert.deepEquals(payload, this.response.data);
	}

	assert(assertLambda) {
		return assertLambda(assert);
	}
}

module.exports = TestResponseWrapper;
