#!/usr/bin/env node

/**
 * Simple helper used by the frontend-maven-plugin to compile the evaluation
 * tool SASS bundle to CSS as part of the Maven build.
 */
const fs = require("fs");
const path = require("path");
const sass = require("sass");
const postcss = require("postcss");
const prefixSelector = require("postcss-prefix-selector");

const args = process.argv.slice(2);

const defaultOutputDir = path.resolve(__dirname, "../../../../target/generated-resources/css");
let outputDir = defaultOutputDir;
let sourceMap = false;

args.forEach((arg, index) => {
  if (arg.startsWith("--outputDir=")) {
    outputDir = path.resolve(process.cwd(), arg.split("=")[1]);
  } else if (arg === "--outputDir") {
    const next = args[index + 1];
    if (next) {
      outputDir = path.resolve(process.cwd(), next);
    }
  } else if (arg === "--sourceMap") {
    sourceMap = true;
  }
});

const ensureDir = (dir) => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
};

const PORTAL_PREFIX = ".Mrphs-sakai-rsf-evaluation";
const DIRECT_PREFIX = ".portletBody.evaluation";

const prefixSelectorForScope = (scope, selector) => {
  if (selector.startsWith(".evaluation")) {
    return selector.replace(/^\.evaluation\b/, scope);
  }
  return `${scope} ${selector}`;
};

const compile = async () => {
  const entryFile = path.resolve(__dirname, "../scss/evaluation/index.scss");
  const result = sass.compile(entryFile, {
    loadPaths: [path.resolve(__dirname, "../scss")],
    style: "expanded",
    sourceMap: sourceMap
  });

  const prefixed = await postcss([
    prefixSelector({
      prefix: PORTAL_PREFIX,
      transform: (prefix, selector) => {
        if (selector.startsWith(PORTAL_PREFIX) || selector.startsWith(DIRECT_PREFIX)) {
          return selector;
        }
        // Avoid prefixing @ rules like @font-face
        if (selector.startsWith("@")) {
          return selector;
        }
        const portalSelector = prefixSelectorForScope(PORTAL_PREFIX, selector);
        const directSelector = prefixSelectorForScope(DIRECT_PREFIX, selector);
        return `${portalSelector}, ${directSelector}`;
      }
    })
  ]).process(result.css, { from: undefined });

  ensureDir(outputDir);
  const cssOutputPath = path.join(outputDir, "evaluation_base.css");
  fs.writeFileSync(cssOutputPath, prefixed.css, "utf8");

  if (sourceMap && result.sourceMap) {
    const mapPath = `${cssOutputPath}.map`;
    fs.writeFileSync(mapPath, JSON.stringify(result.sourceMap), "utf8");
  }
};

compile().catch((err) => {
  console.error(err);
  process.exit(1);
});
