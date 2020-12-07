const { Command } = require('commander');


const fs = require('fs');
const path = require('path');
const log = require('./logger').logger;

const program = new Command();
program
    .version('0.0.1')
    .description('Generates a non-regression test harness for an REST API')
    .requiredOption('-o, --output', 'output the tests into the following folder')
    .option('-u, --url <url>', 'Loads a swagger from an URL')
    .option('-f, --file <path>', 'Loads a swagger from a file');


// must be before .parse()
program.on('--help', () => {
    console.log('');
    console.log('Example call:');
    console.log('  $ speedrester-tool --help');
  });
  
  
program.parse(process.argv);

const swaggerLoader = new SwaggerLoader();
let swaggerInfo = null;

if (program.url) {
    log.info("Loading Swagger from ", program.url);
    swaggerInfo = swaggerLoader.loadFromUrl(program.url);

} else if (program.file) {
    log.info("Loading Swagger from ", program.file);
    swaggerInfo = swaggerLoader.loadFromUrl(program.file);
} else {
    log.error("No option was provided");
    program.help();    
}

if (!swaggerInfo) {
    log.error("Could not load the Swagger, no data have been translated.");
    
}
