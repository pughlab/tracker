gulp =             require 'gulp'
url =              require 'url'
fs =               require 'fs'
path =             require 'path'

require 'coffee-script/register'

gulpCached =       require 'gulp-cached'
gulpLess =         require 'gulp-less'
gulpCoffee =       require 'gulp-coffee'
gulpIgnore =       require 'gulp-ignore'
gulpInject =       require 'gulp-inject'
gulpMocha =        require 'gulp-mocha'
gulpExit =         require 'gulp-exit'
gulpRename =       require 'gulp-rename'
gulpReplace =      require 'gulp-replace'
gulpHtmlmin =      require 'gulp-htmlmin'
gulpServe =        require 'gulp-serve'
gulpNodemon =      require 'gulp-nodemon'
gulpOrder =        require 'gulp-order'
gulpFilter =       require 'gulp-filter'
gulpConcat =       require 'gulp-concat'
gulpUtil =         require 'gulp-util'
gulpUglify =       require 'gulp-uglify'
gulpMinifyCss =    require 'gulp-minify-css'
gulpCsslint =      require 'gulp-csslint'
gulpAngularFilesort = require 'gulp-angular-filesort'
gulpNgHtml2js =    require 'gulp-ng-html2js'
gulpNgAnnotate =   require 'gulp-ng-annotate'

del =              require 'del'
sort =             require 'sort-stream'
queue =            require 'streamqueue'
mainBowerFiles =   require 'main-bower-files'
lazypipe =         require 'lazypipe'
es =               require 'event-stream'
exec =             require('child_process').exec

bower =            require './bower'

karma =            require 'karma'

execute = (command, callback) ->
  exec command, (error, stdout) ->
    callback(stdout)

htmlminOpts =
  removeComments: true
  collapseWhitespace: true
  conservativeCollapse: true
  removeEmptyAttributes: false
  collapseBooleanAttributes: true
  removeRedundantAttributes: true

isWatching = false

compareStrings = (a, b) -> if a < b then -1 else if a > b then 1 else 0


gulp.task 'coffee', () ->
  gulp.src ['./src/main/client/**/*.*coffee', './target/extra-resources/**/*.*coffee']
    .pipe gulpCoffee()
    .pipe gulp.dest('./target/client/tmp/client/statics/app/js')


gulp.task 'bootstrap', () ->
  bootstrapStream = gulp.src('src/main/client/less/custom-bootstrap.less')
  fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*')
  lessPaths = [ 'bower_components/bootstrap/less' ]
  es.merge(
    bootstrapStream
      .pipe gulpLess({paths: lessPaths, verbose: true})
      .pipe gulpRename('bootstrap.css')
      .pipe gulp.dest('./target/client/tmp/client/statics/vendors/css')
    fontsStream
      .pipe gulp.dest('./target/client/tmp/client/statics/vendors/fonts')
  )


gulp.task 'clean-css', (cb) ->
  del './target/client/tmp/client/app.css', cb


gulp.task 'styles', ['clean-css'], () ->
  lessPaths = [
    path.join(__dirname, 'src', 'main', 'client', 'styles')
    path.join(__dirname, 'src', 'main', 'client', 'less')
    path.join(__dirname, 'bower_components', 'bootstrap', 'less')
  ]

  gulp
    .src './src/main/client/app.less'
    .pipe gulpLess {paths: lessPaths}
    .pipe gulp.dest './target/client/tmp/client/statics/app/css/'


gulp.task 'vendors', () ->
  bowerStream = gulp.src(mainBowerFiles(), {base: 'bower_components'})
  coreVendorFiles = bowerStream.pipe(gulpIgnore.exclude(['bower_components/bootstrap/**/*.*', 'bower_components/swagger-ui/**/*.*']))
  swaggerFiles = gulp.src(mainBowerFiles('**/swagger-ui/dist/**/*.*'), {base: 'bower_components/swagger-ui/dist'})
  es.merge(
    coreVendorFiles.pipe(gulpFilter('**/*.css'))
      .pipe(gulp.dest('./target/client/tmp/client/statics/vendors/css'))
    coreVendorFiles.pipe(gulpFilter('**/*.js'))
      .pipe(gulp.dest('./target/client/tmp/client/statics/vendors/js'))
    swaggerFiles
      .pipe(gulp.dest('./target/client/tmp/client/statics/swagger'))
  )


gulp.task 'vendors-test', () ->
  gulp.src(mainBowerFiles({includeDev: true}), {base: 'bower_components'})
    .pipe gulpFilter('**/angular-mocks.js')
    .pipe gulp.dest('./target/client-test/js')


index = () ->
  bowerStream = gulp.src(mainBowerFiles(), {base: 'bower_components', read: false})

  execute 'git log -n 1 --format="%h"', (commitId) ->
    execute 'git diff --shortstat', (shortStat) ->
      shortStat = shortStat.toString().trim()
      commitId = commitId.toString().trim()
      tag = 'Tag: ' + commitId + (if shortStat then (' with: ' + shortStat) else '')

      cssVendorFiles = bowerStream
        .pipe(gulpFilter(['**/*.*', '!bootstrap/**/*.*', '!swagger-ui/**/*.*']))
        .pipe(gulpFilter('**/*.css'))
        .pipe(gulp.dest('./target/client/tmp/client/statics/vendors/css'))

      jsVendorFiles = bowerStream
        .pipe(gulpFilter(['**/*.*', '!bootstrap/**/*.*', '!swagger-ui/**/*.*']))
        .pipe(gulpFilter('**/*.js'))
        .pipe(gulp.dest('./target/client/tmp/client/statics/vendors/js'))

      gulp.src('./src/main/client/index.html')
        .pipe(gulpReplace(/<span class="git-commit">[^<]*?<\/span>/m, '<span class="git-commit">' + tag + '</span>'))
        .pipe(gulpInject(gulp.src('./target/client/tmp/client/statics/vendors/css/bootstrap.css'), {ignorePath: ['target/client/tmp/client'], starttag: '<!-- inject:bootstrap:{{ext}} -->'}))
        .pipe(gulpInject(es.merge(jsVendorFiles, cssVendorFiles), {ignorePath: ['target/client/tmp/client'], starttag: '<!-- inject:vendor:{{ext}} -->'}))
        .pipe(gulpInject(es.merge(appFiles(), cssFiles()), {ignorePath: ['target/client/tmp/client', 'src/main/client']}))
        .pipe(gulp.dest('./target/client/tmp/client/'))


gulp.task 'templates', () ->
  templateFiles({min: true}).pipe(buildTemplates())


gulp.task 'assets', () ->
  gulp.src './src/main/client/assets/statics/**'
    .pipe gulp.dest './target/client/tmp/client/statics'


buildTemplates = () ->
  lazypipe()
    .pipe(gulpNgHtml2js, {
      moduleName: bower.name + '-templates',
      prefix: '/' + bower.name + '/',
      stripPrefix: '/src/app'
    })
    .pipe(gulpConcat, bower.name + '-templates.js')
    .pipe(gulp.dest, './target/client/tmp/client/statics/app/js')()


gulp.task 'build-all', ['styles', 'bootstrap', 'templates', 'coffee', 'vendors', 'assets'], index

gulp.task 'karma-conf', ['templates', 'coffee', 'vendors', 'vendors-test'], () ->

  bowerStream = gulp.src(mainBowerFiles(), {base: 'bower_components', read: false})

  jsVendorFiles = bowerStream
    .pipe(gulpFilter(['**/*.*', '!bootstrap/**/*.*', '!swagger-ui/**/*.*']))
    .pipe(gulpFilter('**/*.js'))
    .pipe(gulp.dest('./target/client/tmp/client/statics/vendors/js'))

  vendorTestFiles = gulp.src('./target/client-test/js/**/*.js')

  karmaFiles = new queue({objectMode: true})
    .queue jsVendorFiles
    .queue vendorTestFiles
    .queue appFiles()
    .queue testFiles()
    .done()

  gulp.src('./src/test/resources/karma.conf.js')
    .pipe gulpInject karmaFiles, {
      starttag: 'files: ['
      endtag: ']'
      addRootSlash: false
      transform: (filepath, file, i, length) -> '\'' + filepath + '\'' + (if i + 1 < length then ',' else '')
    }
    .pipe gulp.dest('./target/karma')


gulp.task 'test', ['karma-conf'], (cb) ->
  callback = (err) ->
    console.log "Errors found" if err?
    cb()
  instance = new karma.Server({
    configFile: __dirname + '/target/karma/karma.conf.js',
    singleRun: true,
    reporters: ['junit']
  }, callback)
  instance.start()

templateFiles = (opt) ->
  gulp.src(['./src/main/client/**/*.html', '!./src/main/client/index.html'], opt)
    .pipe(if opt and opt.min then gulpHtmlmin(htmlminOpts) else gulpUtil.noop())

appVendorFiles = () ->
  gulp.src('./target/client/tmp/client/statics/vendors/**/*.js')

cssVendorFiles = () ->
  gulp.src('./target/client/tmp/client/statics/vendors/css/**/*.css')

appFiles = () ->
  files = [
    './target/client/tmp/client/statics/app/' + bower.name + '-templates.js',
    './target/client/tmp/client/statics/app/**/*.js',
    '!./target/client/tmp/client/**/*_test.js'
  ]
  gulp.src(files)
    .pipe sort (a, b) -> compareStrings(a.relative, b.relative)
    .pipe gulpAngularFilesort()

testFiles = () ->
  files = [
    './target/client/tmp/client/**/*_test.js'
  ]
  gulp.src(files)
    .pipe sort (a, b) -> compareStrings(a.relative, b.relative)
    .pipe gulpAngularFilesort()

cssFiles = (opt) ->
  gulp.src('./target/client/tmp/client/statics/app/css/**/*.css', opt)
    .pipe(gulpOrder([
      'target/client/tmp/client/statics/app/css/app.css'
    ], {base: '.'}))
