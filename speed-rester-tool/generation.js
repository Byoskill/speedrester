/**
 * Several variables are provided for the execution of the generation.
 *
 * log: a logger to print your messages
 * template: a component to load and execute a template with a payload
 *  example: template.nunjucks("templateName", payload") : string
 *  example: template.handlebars("templateName", payload") : string
 * catalog: returns the catalog of objects to be manipulated for the generation.
 * fs: file operations
 *  fs.write("path", content)
 * path: path operations
 * globals : contains a list of global variables declared at the begin of the execution and available in both templates, helpers and partials
 * output: the output folder
 * generationInfo: contains various folder locations
	templates: path.join(argv.generation, 'templates'),
	partials: path.join(argv.generation, 'partials'),
	helpers: path.join(argv.generation, 'helpers'),
    script: path.join(argv.generation, 'generation.js')
 * Handlebars : Template engine
 * script: javascript source file
 * project : the project folder
 * JSONL: JSON to literal
 */


function deepFind(obj, path) {
    var paths = path.split('.')
        , current = obj
        , i;
  
    for (i = 0; i < paths.length; ++i) {
        if (current[paths[i]] == undefined) {
            return undefined;
        } else {
            current = current[paths[i]];
        }
    }
    return current;
}
  

exports.default = class Generation {
    constructor(context) {
        this.context = context;
        this.output = context.output;
        this.log = context.log;
        this.docapi = this.context.catalog;
        this.template = this.context.template;
        this.path = this.context.path;
        this.fs = this.context.fs;
        // Options to generate the code
        this.genOpts = {
            testFolder: this.context.path.join(this.output, '__tests__'),
            entitiesFolder: this.context.path.join(this.output, 'entities'),            
            libFolder: this.context.path.join(this.output, 'lib')
        };

        this.application = {
            author: 'Sylvain Leroy'
        };
    }

    generate() {
        this.log.warn(`script:Rendering project into ${this.project}`);

        this.log.info(`Swagger version is ${this.docapi.swagger}`);

        // The script has the following resources to generate

        // Generates a package.json
        this.log.info('Generates the package.json');
        this.writePackageJson();

        // Generates the folder structure
        this.mkDir(this.genOpts.testFolder);
        this.mkDir(this.genOpts.entitiesFolder);
        this.mkDir(this.genOpts.libFolder);

        // Generate the default config for the REST Client.
        this.generateDefaultParams();

        // Generate the entities
        this.generateEntityFolder();

        // Generate the tests
        this.generateTests();
    }


    generateEntityFolder() {
        // Build param index.
        for (const defKey in this.docapi.definitions) {
            const entityDefinition = this.docapi.definitions[defKey];
            if ( entityDefinition.type === 'object' ) {
                const payload = this.buildPayload(entityDefinition, undefined);
                this.writeFile(this.computePath(`entities/objects/${defKey}.json`), JSON.stringify(payload, null, 2));
            }
        }
    }

    buildPayload(entityDefinition, initialPayload) {
        const payload = initialPayload || {};
        if (!entityDefinition) return payload;
        for( let propKey in entityDefinition.properties) {            
            const props = entityDefinition.properties[propKey];            
            if ( props['$ref'] !== undefined) {
                // 
                payload[propKey] = {};
            } else if ( props.type === 'array') {
                payload[propKey] = [];
            } else {
                payload[propKey] = this.getDefaultValue(props.type);
            }            
        }
        return payload;
    }


    generateDefaultParams() {
        const paths = this.getPaths();
        const queryParamMap = new Map();
        const urlParamMap = new Map();

        // Build param index.
        for (const pathValue in paths) {
            const endpoint = paths[pathValue];
            const endpointInfo = this.getEndpointInfo(endpoint);
            if (!endpointInfo.parameters) continue ;
            for (const param of endpointInfo.parameters) {
                if (param.in === 'path') {
                    urlParamMap.set(param.name, param);
                } else if (param.in === 'query') {
                    queryParamMap.set(param.name, param);
                }
            }
        }

        // Now we produce the structure.
        const defaultParams = { queryParams: {}, pathParams: {}};
        const defaultParamTypes = { queryParams: {}, pathParams: {}};

        var queryParamMapAsc = new Map([...queryParamMap.entries()].sort());
        var urlParamMapAsc = new Map([...urlParamMap.entries()].sort());


        queryParamMapAsc.forEach((v, k) => {
            defaultParams.queryParams[k] = this.guessDefaultValue(v);
            defaultParamTypes.queryParams[k] = v;
        });

        urlParamMapAsc.forEach((v, k) => {
            defaultParams.pathParams[k] = this.guessDefaultValue(v);
            defaultParamTypes.pathParams[k] = v;
        });
        
        this.writeFile(this.computePath('entities/default-params.json'), JSON.stringify(defaultParams, null, 2));
        this.writeFile(this.computePath('entities/default-params-types.json'), JSON.stringify(defaultParamTypes, null, 2));


    }

    guessDefaultValue(v) {
        if ( v.schema && v.schema.value) return v.schema.value;
        if ( !v.required) return null;
        const propType = v.type;
        return this.getDefaultValue(propType);
    }

    getDefaultValue(propType) {
        if ( propType === 'boolean') return false;
        if ( propType === 'string') return '';
        if ( propType === 'integer') return 0;
        if ( propType === 'long') return 0;
        if ( propType === 'array') return [];
        if ( propType === 'object') return {};
        if ( propType === 'ref') return {};
        return null;
    }


    getEndpointInfo(endpoint) {
        return (
            endpoint['get'] ||
            endpoint['head'] ||
            endpoint['post'] ||
            endpoint['put'] ||
            endpoint['trace'] ||
            endpoint['delete'] ||
            endpoint['options']
        );
    }

    getEndpointMethod(endpoint) {        
        return Object.keys(endpoint)[0].toUpperCase();
    }

    getPaths() {
        return this.docapi.paths;
    }

    generateAndWrite(context, template, filename) {
        var content = this.template.handlebars(template, context);
        const outputFilePath = this.computePath(filename);
        this.writeFile(outputFilePath, content);
    }

    writeFile(outputFilePath, content) {
        this.log.info(`Output file path ${outputFilePath}`);
        this.fs.writeFileSync(outputFilePath, content);
    }

    writePackageJson() {
        const context = {
            genOpts: this.genOpts,
            application: this.application
        };
        this.generateAndWrite(context, 'package.json.handlebars', 'package.json');
    }

    computePath(relativeName) {
        const abspath = this.path.join(this.output, relativeName);
        const folderPath = this.path.dirname(abspath);
        if (!this.fs.existsSync(folderPath)) {
            this.fs.mkdirSync(folderPath);
        }
        return abspath;
    }

    mkRelativeDir(relativeFolderPath) {
        const abspath = this.path.join(this.output, relativeFolderPath);
        this.log.info(`Create folder ${abspath}`);
        if (!this.fs.existsSync(abspath)) {
            this.fs.mkdirSync(abspath);
        }
        return abspath;
    }

    mkDir(abspath) {
        this.log.info(`Create folder ${abspath}`);
        if (!this.fs.existsSync(abspath)) {
            this.fs.mkdirSync(abspath);
        }
        return abspath;
    }

    getEndpointResponseType(endpoint) {
        if (endpoint.responses) {
            const value = deepFind(endpoint.responses, '200.schema.type')
            || deepFind(endpoint.responses, '201.schema.type')
            || deepFind(endpoint.responses, '202.schema.type')
            || deepFind(endpoint.responses, '200.schema.$ref')
            || deepFind(endpoint.responses, '201.schema.$ref')
            || deepFind(endpoint.responses, '202.schema.$ref');
            return value;
        }
        return null;
    }


    generateTests() {
        const paths = this.getPaths();
        // 
        // Build param index.         
        for (const pathValue in paths) {
            const endpoint = paths[pathValue];
            const method = this.getEndpointMethod(endpoint);
            const endpointInfo = this.getEndpointInfo(endpoint);
            const controller = endpointInfo.tags[0];
            const pathUrlAsName = pathValue.substring(1).replace(new RegExp('\\{\\}\\/', 'g'), '').replace(/\//g, '_');
            const operationId = controller.operationId || pathUrlAsName.length ==0 ? 'root' : pathUrlAsName; 

            const responseType = this.getEndpointResponseType(endpointInfo);
            const hasBody = ['array', 'object', '$ref'].some(p => p === responseType);
            const isJson = endpointInfo.produces.length > 0 && (endpointInfo.produces[0] === '*/*' || endpointInfo.produces[0] === ('application/json'));

            const pathParams = {};
            const queryParams = {};
            if (endpointInfo.parameters) {
                for (const param of endpointInfo.parameters) {
                    if (param.in === 'path') {
                        pathParams[param.name] = `defaultParams.pathParams.${param.name}`;
                    } else if (param.in === 'query') {
                        queryParams[param.name]= `defaultParams.queryParams.${param.name}`;
                    }
                }
            }

            const context = {
                method,
                controller,
                operationId,
                endpointInfo,
                url: pathValue,
                pathParams,
                hasBody,
                isJson, 
                consumes : endpointInfo.consumes && endpointInfo.consumes[0] || null,
                produces : endpointInfo.produces && endpointInfo.produces[0] || null
            };

            this.generateAndWrite(context, 'nonRegressionTest.js', `__tests__/${controller}/${operationId}.js`);
        }

    }
};