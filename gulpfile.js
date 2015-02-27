/* jshint node: true */
'use strict';

var gulp = require('gulp'),
    fs = require('fs'),
    del = require('del'),
    g = require('gulp-load-plugins')({lazy: false}),
    noop = g.util.noop,
    es = require('event-stream'),
    queue = require('streamqueue'),
    lazypipe = require('lazypipe'),
    stylish = require('jshint-stylish'),
    bower = require('./bower'),
    gulpIgnore = require('gulp-ignore'),
    ngAnnotate = require('gulp-ng-annotate'),
    preprocess = require('gulp-preprocess'),
    sort = require('sort-stream'),
    mainBowerFiles = require('main-bower-files'),
    url = require('url'),
    proxy = require('proxy-middleware'),
    karma = require('karma').server,
    exit = require('gulp-exit'),
    isWatching = false;

function compareStrings(a, b) {
  if (a < b) {
    return -1;
  } else if (a > b) {
    return 1;
  } else {
    return 0;
  }
}

var conditionalPushStateMiddleware = function(req, res, next) {
  var pathname = url.parse(req.url).pathname;
  if (req.url === '/') {
    fs.createReadStream('.tmp/index.html').pipe(res);
  } else if (!pathname.match(/^\/api\//) && !pathname.match(/\.(jpe?g|png|css|js|html?|woff|ttf|svg|map)$/i)) {
    fs.createReadStream('.tmp/index.html').pipe(res);
  } else {
    next();
  }
};

var proxyOptions = url.parse('http://localhost:3001/api');
proxyOptions.route = '/api';
proxyOptions.cookieRewrite = true;

var htmlminOpts = {
  removeComments: true,
  collapseWhitespace: true,
  conservativeCollapse: true,
  removeEmptyAttributes: false,
  collapseBooleanAttributes: true,
  removeRedundantAttributes: true
};

/**
 * JS Hint
 */
gulp.task('jshint', function () {
  return gulp.src([
    './gulpfile.js',
    './src/app/**/*.js'
  ])
    .pipe(g.cached('jshint'))
    .pipe(jshint('./.jshintrc'))
    .pipe(livereload());
});

/**
 * CSS
 */
gulp.task('clean-css', function (cb) {
  del(['./.tmp/css'], cb);
});

gulp.task('styles', ['clean-css'], function () {
  return gulp.src([
    './src/app/**/*.scss',
    '!./src/app/**/_*.scss'
  ])
    .pipe(g.sass())
    .pipe(gulp.dest('./.tmp/css/'))
    .pipe(g.cached('built-css'))
    .pipe(livereload());
});

gulp.task('styles-dist', ['styles'], function () {
  return cssFiles().pipe(dist('css', bower.name));
});

gulp.task('csslint', ['styles'], function () {
  return cssFiles()
    .pipe(gulpIgnore.exclude(/bootstrap\.css$/))
    .pipe(g.cached('csslint'))
    .pipe(g.csslint('./.csslintrc'))
    .pipe(g.csslint.reporter());
});

/**
 * CoffeeScript
 */
gulp.task('coffee', function () {
  return gulp.src([
    './src/app/**/*.*coffee'
  ])
    .pipe(preprocess({context: { NODE_ENV: 'development' }}))
    .pipe(g.coffee())
    .pipe(gulp.dest('./.tmp/src/app'));
});

gulp.task('coffee-service', function () {
  return gulp.src([
    './src/service/**/*.*coffee'
  ])
    .pipe(g.coffee({bare: true}))
    .pipe(gulp.dest('./.tmp/src/service'));
});

gulp.task('coffee-knex', function () {
  return es.merge(
    gulp.src(['./migrations/**/*.*coffee']).pipe(g.coffee({bare: true})).pipe(gulp.dest('./.tmp/src/knex/migrations')),
    gulp.src(['./seeds/**/*.*coffee']).pipe(g.coffee({bare: true})).pipe(gulp.dest('./.tmp/src/knex/seeds'))
  );
});

gulp.task('test-service-resources', function () {
  return gulp.src([
    './src/test/**/*.*'
  ])
    .pipe(gulp.dest('./.tmp/src/test'));
});

gulp.task('test-service', function () {
  return gulp.src([
    './src/service/**/*_test.coffee',
  ], {read: false})
    .pipe(g.mocha({reporter: 'spec'}))
    .on('error', function(err) { console.log(err.stack); })
    .pipe(exit());
});

gulp.task('service-dist', ['coffee-service'], function() {
  return serviceFiles().pipe(gulp.dest('./dist'));
});

/**
 * Scripts
 */
gulp.task('scripts-dist', ['templates-dist'], function () {

  return es.merge(
    gulp.src([
      './src/app/**/*.*coffee',
      '!./src/app/**/*_test.*coffee'
    ])
      .pipe(preprocess({context: { NODE_ENV: 'production' }}))
      .pipe(g.coffee()),
    gulp.src([
      './src/app/**/*.html', '!./src/app/index.html'])
      .pipe(preprocess({context: { NODE_ENV: 'production' }}))
      .pipe(g.htmlmin(htmlminOpts))
      .pipe(g.ngHtml2js({ moduleName: bower.name + '-templates', prefix: '/' + bower.name + '/', stripPrefix: '/src/app' }))
      .pipe(g.concat(bower.name + '-templates.js'))
  )
    .pipe(sort(function (a, b) { return compareStrings(a.relative, b.relative); }))
    .pipe(g.angularFilesort())
    .pipe(dist('js', bower.name, {ngmin: true}));
});

/**
 * Templates
 */
gulp.task('templates', function () {
  return templateFiles().pipe(buildTemplates());
});

gulp.task('templates-dist', function () {
  return templateFiles({min: true}).pipe(buildTemplates());
});

/**
 * Bootstrap
 */
gulp.task('bootstrap', function () {
  var bootstrapStream = gulp.src('src/app/less/custom-bootstrap.less');
  var fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*');
  var lessPaths = [ 'bower_components/bootstrap/less' ];
  return es.merge(
    bootstrapStream.pipe(g.less({paths: lessPaths, verbose: true})).pipe(g.rename('bootstrap.css')).pipe(gulp.dest('./.tmp/css')),
    fontsStream.pipe(gulp.dest('./.tmp/fonts'))
  );
});

/**
 * Vendors
 */
gulp.task('vendors', function () {
  var bowerStream = gulp.src(mainBowerFiles()).pipe(g.ignore.exclude('bower_components/bootstrap/**/*.*'));
  var bootstrapStream = gulp.src('bower_components/bootstrap/less/bootstrap.less');
  var fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*');
  var lessPaths = [ 'src/app/less', 'bower_components/bootstrap/less' ];
  return es.merge(
    es.merge(
      bootstrapStream.pipe(g.rename({dirname: 'src/app/less'})).pipe(g.less({paths: lessPaths})),
      bowerStream.pipe(g.filter('**/*.css'))
    ).pipe(dist('css', 'vendors')),
    bowerStream.pipe(g.filter('**/*.js')).pipe(dist('js', 'vendors', {ngmin: true})),
    fontsStream.pipe(gulp.dest('./dist/client/statics/fonts'))
  );
});

/**
 * Index
 */
gulp.task('index', index);
gulp.task('build-all', ['styles', 'bootstrap', 'templates', 'coffee', 'coffee-service'], index);
function index () {
  var opt = {read: false};
  var cssSortFunction = function(a, b) {
    if (a.path.endsWith('/bootstrap.css')) {
      return -1;
    } else if (b.path.endsWith('/bootstrap.css')) {
      return 1;
    } else {
      return a.path.localeCompare(b.path);
    }
  };
  return gulp.src('./src/app/index.html')
    .pipe(g.inject(gulp.src(mainBowerFiles(opt), {read: false}).pipe(gulpIgnore.exclude('bower_components/bootstrap/**/*.*')), {ignorePath: 'bower_components', starttag: '<!-- inject:vendor:{{ext}} -->'}))
    .pipe(g.inject(es.merge(appFiles(), cssFiles(opt).pipe(sort(cssSortFunction))), {ignorePath: ['.tmp', 'src/app']}))
    .pipe(gulp.dest('./src/app/'))
    .pipe(g.embedlr())
    .pipe(gulp.dest('./.tmp/'))
    .pipe(livereload());
}

/**
 * Assets
 */
gulp.task('assets', function () {
  return gulp.src('./src/app/assets/statics/**')
    .pipe(gulp.dest('./dist/client/statics'));
});

/**
 * Knex.js for dist
 */
gulp.task('knex-dist', ['coffee-knex'], function () {
  return gulp.src('./.tmp/src/knex/**')
    .pipe(gulp.dest('./dist/knex'));
});

/**
 * Dist
 */
gulp.task('dist', ['vendors', 'assets', 'styles-dist', 'scripts-dist', 'service-dist', 'knex-dist'], function () {
  return gulp.src('./src/app/index.html')
    .pipe(g.inject(gulp.src('./dist/client/statics/{bootstrap,vendors}.min.{js,css}'), {ignorePath: 'dist/client', starttag: '<!-- inject:vendor:{{ext}} -->'}))
    .pipe(g.inject(gulp.src('./dist/client/statics/' + bower.name + '.min.{js,css}'), {ignorePath: 'dist/client'}))
    .pipe(g.htmlmin(htmlminOpts))
    .pipe(gulp.dest('./dist/client/'));
});

gulp.task('clean', function (cb) {
  del(['./.tmp'], cb);
});

gulp.task('clean-dist', function (cb) {
  del(['./dist'], cb);
});

/**
 * Static file server
 */
gulp.task('statics', g.serve({
  port: 3000,
  root: ['./.tmp', './.tmp/src/app', './src/app/assets', './bower_components'],
  middlewares: [conditionalPushStateMiddleware, proxy(proxyOptions)]
}));

/**
 * Watch
 */
gulp.task('serve', ['watch']);
gulp.task('watch', ['statics', 'default'], function () {
  isWatching = true;
  // Initiate livereload server:
  g.livereload();
  gulp.watch('./src/app/**/*.*coffee', ['coffee']).on('change', function (evt) {
    if (evt.type !== 'changed') {
      gulp.start('index');
    }
  });
  gulp.watch('./src/service/**/*.*coffee', ['coffee-service']);
  gulp.watch('./src/app/index.html', ['index']);
  gulp.watch('./src/app/less/**/*.less', ['bootstrap']);
  gulp.watch(['./src/app/**/*.html', '!./src/app/index.html'], ['templates']);
  gulp.watch(['./src/app/**/*.scss'], ['csslint', 'bootstrap']).on('change', function (evt) {
    if (evt.type !== 'changed') {
      gulp.start('index');
    }
  });
  g.nodemon({ script: '.tmp/src/service/main.js', watch: ['.tmp/src/service'] })
    .on('restart', function () {
      console.log('restarted!');
    });
});

var conditionalPushStateVerifyMiddleware = function(req, res, next) {
  var pathname = url.parse(req.url).pathname;
  if (req.url === '/') {
    fs.createReadStream('dist/client/index.html').pipe(res);
  } else if (!pathname.match(/^\/api\//) && !pathname.match(/\.(jpe?g|png|css|js|html?|woff|ttf|svg|map)$/i)) {
    fs.createReadStream('dist/client/index.html').pipe(res);
  } else {
    next();
  }
};

gulp.task('verify-statics', g.serve({
  port: 3000,
  root: ['./dist/client'],
  middlewares: [conditionalPushStateVerifyMiddleware, proxy(proxyOptions)]
}));

gulp.task('verify', ['verify-statics'], function() {
  g.nodemon({ script: './dist/main.js' })
    .on('restart', function () {
      console.log('restarted!');
    });
});

/**
 * Default task
 */
gulp.task('default', ['lint', 'build-all']);

/**
 * Lint everything
 */
gulp.task('lint', ['jshint', 'csslint']);

/**
 * Test
 */
gulp.task('test', ['templates', 'coffee', 'karma-conf'], function (done) {
  karma.start({
    configFile: __dirname + '/karma.conf.js',
    singleRun: true
  }, done);
});

gulp.task('test-all', ['test', 'test-service']);

/**
 * Inject all files for tests into karma.conf.js
 * to be able to run `karma` without gulp.
 */
gulp.task('karma-conf', ['templates'], function () {
  return gulp.src('./karma.conf.js')
    .pipe(g.inject(testFiles(), {
      starttag: 'files: [',
      endtag: ']',
      addRootSlash: false,
      transform: function (filepath, file, i, length) {
        return '\'' + filepath + '\'' + (i + 1 < length ? ',' : '');
      }
    }))
    .pipe(gulp.dest('./'));
});

/**
 * Test files
 */
function testFiles() {
  return new queue({objectMode: true})
    .queue(gulp.src(mainBowerFiles(), {read: false}).pipe(gulpIgnore.exclude('!**/*.js')))
    .queue(gulp.src('./bower_components/angular-mocks/angular-mocks.js'))
    .queue(appFiles())
    .queue(gulp.src(['./src/app/**/*_test.js', './.tmp/src/app/**/*_test.js']))
    .done();
}

/**
 * All CSS files as a stream
 */
function cssFiles (opt) {
  return gulp.src('./.tmp/css/**/*.css', opt);
}

/**
 * All AngularJS application files as a stream
 */
function appFiles () {
  var files = [
    './.tmp/src/app/**/*.js',
    '!./.tmp/src/app/**/*_test.js'
  ];
  return gulp.src(files)
    .pipe(sort(function (a, b) { return compareStrings(a.relative, b.relative); }))
    .pipe(g.angularFilesort());
}

/**
 * The service files as a stream
 */
function serviceFiles() {
  return gulp.src(['./package.json', './.tmp/src/service/**/*.js', './.tmp/src/service/**/*.json', '!./.tmp/src/service/**/*_test.js']);
}

/**
 * All AngularJS templates/partials as a stream
 */
function templateFiles (opt) {
  return gulp.src(['./src/app/**/*.html', '!./src/app/index.html'], opt)
    .pipe(opt && opt.min ? g.htmlmin(htmlminOpts) : noop());
}

/**
 * Build AngularJS templates/partials
 */
function buildTemplates () {
  return lazypipe()
    .pipe(g.ngHtml2js, {
      moduleName: bower.name + '-templates',
      prefix: '/' + bower.name + '/',
      stripPrefix: '/src/app'
    })
    .pipe(g.concat, bower.name + '-templates.js')
    .pipe(gulp.dest, './.tmp/src/app')
    .pipe(livereload)();
}

/**
 * Concat, rename, minify
 *
 * @param {String} ext
 * @param {String} name
 * @param {Object} opt
 */
function dist (ext, name, opt) {
  opt = opt || {};
  return lazypipe()
    .pipe(g.concat, name + '.' + ext)
    .pipe(gulp.dest, './dist/client/statics')
    .pipe(opt.ngmin ? ngAnnotate : noop)
    .pipe(opt.ngmin ? g.rename : noop, name + '.annotated.' + ext)
    .pipe(opt.ngmin ? gulp.dest : noop, './dist/client/statics')
    .pipe(ext === 'js' ? g.uglify : g.minifyCss)
    .pipe(g.rename, name + '.min.' + ext)
    .pipe(gulp.dest, './dist/client/statics')();
}

/**
 * Livereload (or noop if not run by watch)
 */
function livereload () {
  return lazypipe()
    .pipe(isWatching ? g.livereload : noop)();
}

/**
 * Jshint with stylish reporter
 */
function jshint (jshintfile) {
  return lazypipe()
    .pipe(g.jshint, jshintfile)
    .pipe(g.jshint.reporter, stylish)();
}