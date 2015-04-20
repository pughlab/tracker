gulp =           require('gulp')
fs =             require('fs')
del =            require('del')
g =              require('gulp-load-plugins')({lazy: false})
noop =           g.util.noop
es =             require('event-stream')
queue =          require('streamqueue')
lazypipe =       require('lazypipe')
bower =          require('./bower')
gulpIgnore =     require('gulp-ignore')
ngAnnotate =     require('gulp-ng-annotate')
preprocess =     require('gulp-preprocess')
sort =           require('sort-stream')
mainBowerFiles = require('main-bower-files')
url =            require('url')
proxy =          require('proxy-middleware')
karma =          require('karma').server
exit =           require('gulp-exit')
exec =           require('child_process').exec
isWatching =     false

execute = (command, callback) ->
  exec command, (error, stdout) ->
    callback(stdout)

compareStrings = (a, b) ->
  if a < b
    -1
  else if a > b
    1
  else
    0

conditionalPushStateMiddleware = (req, res, next) ->
  pathname = url.parse(req.url).pathname
  if req.url == '/'
    fs.createReadStream('.tmp/index.html').pipe(res)
  else if !pathname.match(/^\/api\//) && !pathname.match(/\.(jpe?g|png|css|js|html?|woff|ttf|svg|map)$/i)
    fs.createReadStream('.tmp/index.html').pipe(res)
  else
    next()

proxyOptions = url.parse('http://localhost:3001/api')
proxyOptions.route = '/api'
proxyOptions.cookieRewrite = true

htmlminOpts =
  removeComments: true
  collapseWhitespace: true
  conservativeCollapse: true
  removeEmptyAttributes: false
  collapseBooleanAttributes: true
  removeRedundantAttributes: true

##
## CSS
##
gulp.task 'clean-css', (cb) ->
  del(['./.tmp/css'], cb)

gulp.task 'styles', ['clean-css'], () ->
  gulp.src([ './src/app/**/*.scss', '!./src/app/**/_*.scss' ])
    .pipe g.sass()
    .pipe gulp.dest('./.tmp/css/')
    .pipe g.cached('built-css')
    .pipe livereload()

gulp.task 'styles-dist', ['styles'], () ->
  cssFiles()
    .pipe dist('css', bower.name)()

gulp.task 'csslint', ['styles'], () ->
  cssFiles()
    .pipe gulpIgnore.exclude(/bootstrap\.css$/)
    .pipe g.cached('csslint')
    .pipe g.csslint('./.csslintrc')
    .pipe g.csslint.reporter()

##
## CoffeeScript
##
gulp.task 'coffee', () ->
  gulp.src([ './src/app/**/*.*coffee' ])
    .pipe preprocess({context: { NODE_ENV: 'development' }})
    .pipe g.coffee()
    .pipe gulp.dest('./.tmp/src/app')

gulp.task 'coffee-service', () ->
  gulp.src([ './src/service/**/*.*coffee' ])
    .pipe g.coffee({bare: true})
    .pipe gulp.dest('./.tmp/src/service')

gulp.task 'coffee-knex', () ->
  es.merge(
    gulp.src ['./migrations/**/*.*coffee']
      .pipe g.coffee({bare: true})
      .pipe gulp.dest('./.tmp/src/knex/migrations')
    gulp.src ['./seeds/**/*.*coffee']
      .pipe g.coffee({bare: true})
      .pipe gulp.dest('./.tmp/src/knex/seeds')
  )

gulp.task 'test-service-resources', () ->
  gulp.src([ './src/test/**/*.*' ])
    .pipe gulp.dest('./.tmp/src/test')

gulp.task 'test-service', () ->
  gulp.src([ './src/service/**/*_test.coffee', ], {read: false})
    .pipe g.mocha({reporter: 'spec'})
    .on 'error', (err) -> console.log(err.stack)
    .pipe exit()


gulp.task 'service-dist', ['coffee-service'],() ->
  serviceFiles()
    .pipe(gulp.dest('./dist'))

##
## Scripts
##
gulp.task 'scripts-dist', ['templates-dist'], () ->

  es.merge(
    gulp.src([ './src/app/**/*.*coffee', '!./src/app/**/*_test.*coffee' ])
      .pipe preprocess({context: { NODE_ENV: 'production' }})
      .pipe g.coffee()
    gulp.src([ './src/app/**/*.html', '!./src/app/index.html'])
      .pipe preprocess({context: { NODE_ENV: 'production' }})
      .pipe g.htmlmin(htmlminOpts)
      .pipe g.ngHtml2js({ moduleName: bower.name + '-templates', prefix: '/' + bower.name + '/', stripPrefix: '/src/app' })
      .pipe g.concat(bower.name + '-templates.js')
  )
    .pipe sort (a, b) -> compareStrings(a.relative, b.relative)
    .pipe g.angularFilesort()
    .pipe dist('js', bower.name, {ngmin: true})()


##
## Templates
##
gulp.task 'templates', () ->
  templateFiles()
    .pipe(buildTemplates())

gulp.task 'templates-dist', () ->
  templateFiles({min: true})
    .pipe(buildTemplates())

##
## Bootstrap
##
gulp.task 'bootstrap', () ->
  bootstrapStream = gulp.src('src/app/less/custom-bootstrap.less')
  fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*')
  lessPaths = [ 'bower_components/bootstrap/less' ]
  es.merge(
    bootstrapStream
      .pipe g.less({paths: lessPaths, verbose: true})
      .pipe g.rename('bootstrap.css')
      .pipe gulp.dest('./.tmp/css')
    fontsStream
      .pipe gulp.dest('./.tmp/fonts')
  )

##
## Vendors
##
gulp.task 'vendors', () ->
  bowerStream = gulp.src(mainBowerFiles()).pipe(g.ignore.exclude('bower_components/bootstrap/**/*.*'))
  bootstrapStream = gulp.src('bower_components/bootstrap/less/bootstrap.less')
  fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*')
  lessPaths = [ 'src/app/less', 'bower_components/bootstrap/less' ]
  cssFiles = es.merge(
    bootstrapStream
      .pipe g.rename({dirname: 'src/app/less'})
      .pipe g.less({paths: lessPaths})
    bowerStream
      .pipe g.filter('**/*.css')
  )
  es.merge(
    cssFiles
      .pipe dist('css', 'vendors')()
    bowerStream
      .pipe g.filter('**/*.js')
      .pipe dist('js', 'vendors', {ngmin: true})()
    fontsStream
      .pipe gulp.dest('./dist/client/statics/fonts')
  )

##
## Index
##
gulp.task 'index', () ->
  index()
gulp.task 'build-all', ['styles', 'bootstrap', 'templates', 'coffee', 'coffee-service'], () ->
  index()

index = () ->
  opt = {read: false}

  cssSortFunction = (a, b) ->
    if a.path.endsWith('/bootstrap.css')
      -1
    else if b.path.endsWith('/bootstrap.css')
      1
    else
      a.path.localeCompare(b.path)
  
  execute 'git log -n 1 --format="%h"', (commitId) ->
    execute 'git diff --shortstat', (shortStat) ->
      shortStat = shortStat.toString().trim()
      commitId = commitId.toString().trim()
      tag = 'Tag: ' + commitId + (if shortStat then (' with: ' + shortStat) else '')
      gulp.src('./src/app/index.html')
        .pipe g.replace(/<span class="git-commit">[^<]*?<\/span>/m, '<span class="git-commit">' + tag + '</span>')
        .pipe g.inject(gulp.src(mainBowerFiles(opt), {read: false}).pipe(gulpIgnore.exclude('bower_components/bootstrap/**/*.*')), {ignorePath: 'bower_components', starttag: '<!-- inject:vendor:{{ext}} -->'})
        .pipe g.inject(es.merge(appFiles(), cssFiles(opt).pipe(sort(cssSortFunction))), {ignorePath: ['.tmp', 'src/app']})
        .pipe g.embedlr()
        .pipe gulp.dest('./.tmp/')
        .pipe livereload()

##
## Assets
##
gulp.task 'assets', () ->
  gulp.src('./src/app/assets/statics/**')
    .pipe gulp.dest('./dist/client/statics')

##
## Knex.js for dist
##
gulp.task 'knex-dist', ['coffee-knex'], () ->
  gulp.src('./.tmp/src/knex/**')
    .pipe gulp.dest('./dist/knex')

##
## It's handy to have a production version that doesn't minify and concatenate everything.
## Although this is work to maintain in parallel, we can't really diagnose easily without
## Using a totally different dist process.
##

gulp.task 'debug-angular-dist', () ->
  es.merge(
    gulp.src([ './src/app/**/*.*coffee', '!./src/app/**/*_test.*coffee' ])
      .pipe preprocess({context: { NODE_ENV: 'production' }})
      .pipe g.coffee()
    gulp.src([ './src/app/**/*.html', '!./src/app/index.html'])
      .pipe preprocess({context: { NODE_ENV: 'production' }})
      .pipe g.ngHtml2js({ moduleName: bower.name + '-templates', prefix: '/' + bower.name + '/', stripPrefix: '/src/app' })
      .pipe g.concat(bower.name + '-templates.js')
  )
  .pipe sort (a, b) -> compareStrings(a.relative, b.relative)
  .pipe ngAnnotate()
  .pipe gulp.dest('./dist/client/statics/app')


gulp.task 'debug-dist', ['assets', 'debug-angular-dist', 'service-dist', 'knex-dist'],  () ->

  cssSortFunction = (a, b) ->
    if a.path.endsWith('/bootstrap.css')
      -1
    else if b.path.endsWith('/bootstrap.css')
      1
    else
      a.path.localeCompare(b.path)

  bowerStream = gulp.src(mainBowerFiles(), { base: './bower_components'}).pipe(g.ignore.exclude('bower_components/bootstrap/**/*.*'))
  bootstrapStream = gulp.src('bower_components/bootstrap/less/bootstrap.less')
  fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*')
  lessPaths = [ 'src/app/less', 'bower_components/bootstrap/less' ]
  
  vendorCssFiles = es.merge(
    bootstrapStream
      .pipe g.less({paths: lessPaths})
    bowerStream
      .pipe g.filter('**/*.css')
  )
  .pipe(g.order([ "bootstrap/less/bootstrap.css", "**/*.*" ], {base: 'bower_components'}))
  .pipe(gulp.dest('./dist/client/statics/vendors'))

  appCssFiles = gulp.src([ './src/app/**/*.scss', '!./src/app/**/_*.scss' ])
    .pipe g.sass()
    .pipe gulp.dest('./dist/client/statics/app')

  bowerFiles = es.merge(
    vendorCssFiles
    bowerStream
      .pipe g.filter('**/*.js')
      .pipe gulp.dest('./dist/client/statics/vendors')
    fontsStream
      .pipe gulp.dest('./dist/client/statics/fonts')
  )

  angularFiles = gulp.src([ './dist/client/statics/app/**/*.js' ])
    .pipe g.angularFilesort()

  execute 'git log -n 1 --format="%h"', (commitId) ->
    execute 'git diff --shortstat', (shortStat) ->
      shortStat = shortStat.toString().trim()
      commitId = commitId.toString().trim()
      tag = 'Tag: ' + commitId + (if shortStat then (' with: ' + shortStat) else '')

      injectVendorFiles = bowerFiles
      injectAppFiles = es.merge(angularFiles, appCssFiles)

      es.merge(
        bowerFiles
        appCssFiles
        gulp.src('./src/app/index.html')
          .pipe g.replace(/<span class="git-commit">[^<]*?<\/span>/m, '<span class="git-commit">' + tag + '</span>')
          .pipe g.inject(injectVendorFiles, {ignorePath: 'dist/client', starttag: '<!-- inject:vendor:{{ext}} -->'})
          .pipe g.inject(injectAppFiles, {ignorePath: 'dist/client', starttag: '<!-- inject:{{ext}} -->'})
          .pipe gulp.dest('./dist/client')
      )


##
## Dist
##

gulp.task 'dist', ['vendors', 'assets', 'styles-dist', 'scripts-dist', 'service-dist', 'knex-dist'],  () ->
  execute 'git log -n 1 --format="%h"', (commitId) ->
    execute 'git diff --shortstat', (shortStat) ->
      shortStat = shortStat.toString().trim()
      commitId = commitId.toString().trim()
      tag = 'Tag: ' + commitId + (if shortStat then (' with: ' + shortStat) else '')
      gulp.src('./src/app/index.html')
        .pipe g.replace(/<span class="git-commit">[^<]*?<\/span>/m, '<span class="git-commit">' + tag + '</span>')
        .pipe g.inject(gulp.src('./dist/client/statics/{bootstrap,vendors}.min.{js,css}'), {ignorePath: 'dist/client', starttag: '<!-- inject:vendor:{{ext}} -->'})
        .pipe g.inject(gulp.src('./dist/client/statics/' + bower.name + '.min.{js,css}'), {ignorePath: 'dist/client'})
        .pipe g.htmlmin(htmlminOpts)
        .pipe gulp.dest('./dist/client/')

gulp.task 'clean', (cb) ->
  del(['./.tmp'], cb)

gulp.task 'clean-dist', (cb) ->
  del(['./dist'], cb)

##
## Static file server
##
gulp.task 'statics', g.serve({
  port: 3000,
  root: ['./.tmp', './.tmp/src/app', './src/app/assets', './bower_components']
  middlewares: [conditionalPushStateMiddleware, proxy(proxyOptions)]
})

##
## Watch
##
gulp.task 'serve', ['watch']
gulp.task 'watch', ['statics', 'default'], () ->
  isWatching = true
  ## Initiate livereload server:
  g.livereload()
  gulp.watch './src/app/**/*.*coffee', ['coffee']
    .on 'change', (evt) ->
      if evt.type != 'changed'
        gulp.start('index')

  gulp.watch './src/service/**/*.*coffee', ['coffee-service']
  gulp.watch './src/app/index.html', ['index']
  gulp.watch './src/app/less/**/*.less', ['bootstrap']
  gulp.watch ['./src/app/**/*.html', '!./src/app/index.html'], ['templates']
  gulp.watch ['./src/app/**/*.scss'], ['csslint', 'bootstrap']
    .on 'change', (evt) ->
      if evt.type != 'changed'
        gulp.start('index')

  g.nodemon { script: '.tmp/src/service/main.js', watch: ['.tmp/src/service'] }
    .on 'restart', () -> console.log('restarted!')

conditionalPushStateVerifyMiddleware = (req, res, next) ->
  pathname = url.parse(req.url).pathname
  if req.url == '/'
    fs.createReadStream('dist/client/index.html').pipe(res)
  else if !pathname.match(/^\/api\//) && !pathname.match(/\.(jpe?g|png|css|js|html?|woff|ttf|svg|map)$/i)
    fs.createReadStream('dist/client/index.html').pipe(res)
  else
    next()

gulp.task 'verify-statics', g.serve({
  port: 3000
  root: ['./dist/client']
  middlewares: [conditionalPushStateVerifyMiddleware, proxy(proxyOptions)]
})

gulp.task 'verify', ['verify-statics'], () ->
  g.nodemon { script: './dist/main.js' }
    .on 'restart', () -> console.log('restarted!')

##
## Default task
##
gulp.task 'default', ['lint', 'build-all']

##
## Lint everything
##
gulp.task 'lint', ['csslint']

##
## Test
##
gulp.task 'test', ['templates', 'coffee', 'karma-conf'], (done) ->
  karma.start({
    configFile: __dirname + '/karma.conf.js',
    singleRun: true
  }, done)


gulp.task 'test-all', ['test', 'test-service']


##
## Inject all files for tests into karma.conf.js
## to be able to run `karma` without gulp.
##
gulp.task 'karma-conf', ['templates'], () ->
  gulp.src('./karma.conf.js')
    .pipe g.inject testFiles(), {
      starttag: 'files: ['
      endtag: ']'
      addRootSlash: false
      transform: (filepath, file, i, length) -> '\'' + filepath + '\'' + (if i + 1 < length then ',' else '')
    }
    .pipe gulp.dest('./')

##
## Test files
##
testFiles = () ->
  new queue({objectMode: true})
    .queue gulp.src(mainBowerFiles(), {read: false}).pipe(gulpIgnore.exclude('!**/*.js'))
    .queue gulp.src('./bower_components/angular-mocks/angular-mocks.js')
    .queue appFiles()
    .queue gulp.src(['./src/app/**/*_test.js', './.tmp/src/app/**/*_test.js'])
    .done()

##
## All CSS files as a stream
##
cssFiles = (opt) ->
  gulp.src('./.tmp/css/**/*.css', opt)

##
## All AngularJS application files as a stream
##
appFiles = () ->
  files = [ './.tmp/src/app/**/*.js', '!./.tmp/src/app/**/*_test.js']
  gulp.src(files)
    .pipe sort (a, b) -> compareStrings(a.relative, b.relative)
    .pipe g.angularFilesort()

##
## The service files as a stream
##
serviceFiles = () ->
  gulp.src(['./package.json', './.tmp/src/service/**/*.js', './.tmp/src/service/**/*.json', '!./.tmp/src/service/**/*_test.js'])

##
## All AngularJS templates/partials as a stream
##
templateFiles = (opt) ->
  gulp
    .src ['./src/app/**/*.html', '!./src/app/index.html'], opt
    .pipe (if opt?.min then g.htmlmin(htmlminOpts) else noop())

##
## Build AngularJS templates/partials
##
buildTemplates = () ->
  lazypipe()
    .pipe(g.ngHtml2js, {
      moduleName: bower.name + '-templates',
      prefix: '/' + bower.name + '/',
      stripPrefix: '/src/app'
    })
    .pipe g.concat, bower.name + '-templates.js'
    .pipe gulp.dest, './.tmp/src/app'
    .pipe(livereload)()

##
## Concat, rename, minify
##
## @param {String} ext
## @param {String} name
## @param {Object} opt
##

dist = (ext, name, opt) ->
  opt = opt || {}
  lazypipe()
    .pipe(g.concat, name + '.' + ext)
    .pipe(gulp.dest, './dist/client/statics')
    .pipe (if opt.ngmin then ngAnnotate else noop)
    .pipe (if opt.ngmin then g.rename else noop), name + '.annotated.' + ext
    .pipe (if opt.ngmin then gulp.dest else noop), './dist/client/statics'
    .pipe (if ext == 'js' then g.uglify else g.minifyCss)
    .pipe g.rename, name + '.min.' + ext
    .pipe gulp.dest, './dist/client/statics'

##
## Livereload (or noop if not run by watch)
##
livereload = () ->
  lazypipe()
    .pipe(if isWatching then g.livereload else noop)()
