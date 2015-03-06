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
gulpRename =       require 'gulp-rename'
gulpHtmlmin =      require 'gulp-htmlmin'
gulpServe =        require 'gulp-serve'
gulpNodemon =      require 'gulp-nodemon'
gulpKarma =        require 'gulp-karma'
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

bower =            require './bower'

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
  gulp.src ['./src/main/client/**/*.*coffee']
    .pipe gulpCoffee()
    .pipe gulp.dest('./target/client/tmp/app/js')

gulp.task 'bootstrap', () ->
  bootstrapStream = gulp.src('src/main/client/less/custom-bootstrap.less')
  fontsStream = gulp.src('bower_components/bootstrap/fonts/*.*')
  lessPaths = [ 'bower_components/bootstrap/less' ]
  es.merge(
    bootstrapStream
      .pipe gulpLess({paths: lessPaths, verbose: true})
      .pipe gulpRename('bootstrap.css')
      .pipe gulp.dest('./target/client/tmp/vendors/css')
    fontsStream
      .pipe gulp.dest('./target/client/tmp/app/statics/fonts')
  )

gulp.task 'clean-css', (cb) ->
  del './target/client/tmp/app.css/app.css', cb

gulp.task 'styles', ['clean-css'], () ->
  lessPaths = [
    path.join(__dirname, 'src', 'main', 'client', 'styles')
    path.join(__dirname, 'src', 'main', 'client', 'less')
    path.join(__dirname, 'bower_components', 'bootstrap', 'less')
  ]

  gulp
    .src './src/main/client/app.less'
    .pipe gulpLess {paths: lessPaths}
    .pipe gulp.dest './target/client/tmp/app/css/'

gulp.task 'vendors', () ->
  bowerStream = gulp.src(mainBowerFiles()).pipe(gulpIgnore.exclude('bower_components/bootstrap/**/*.*'))
  es.merge(
    bowerStream.pipe(gulpFilter('**/*.css'))
      .pipe(gulpConcat('vendors.css'))
      .pipe(gulp.dest('./target/client/tmp/vendors/css'))
    bowerStream.pipe(gulpFilter('**/*.js'))
      .pipe(gulpConcat('vendors.js'))
      .pipe(gulp.dest('./target/client/tmp/vendors/js')))

index = () ->
  opt = {read: false}
  cssSortFunction = (a, b) ->
    if a.path.endsWith('/bootstrap.css')
      -1
    else if b.path.endsWith('/bootstrap.css')
      1
    else
      a.path.localeCompare(b.path)

  gulp.src('./src/main/client/index.html')
    .pipe(gulpInject(es.merge(appVendorFiles(), cssVendorFiles(opt).pipe(sort(cssSortFunction))), {ignorePath: ['target/client/tmp'], starttag: '<!-- inject:vendor:{{ext}} -->'}))
    .pipe(gulpInject(es.merge(appFiles(), cssFiles(opt).pipe(sort(cssSortFunction))), {ignorePath: ['target/client/tmp', 'src/main/client']}))
    .pipe(gulp.dest('./target/client/tmp/'))
    
gulp.task 'templates', () ->
  templateFiles({min: true}).pipe(buildTemplates())
  
gulp.task 'assets', () ->
  gulp.src './src/main/client/assets/statics/**'
    .pipe gulp.dest './target/client/tmp/statics'
  
buildTemplates = () ->
  lazypipe()
    .pipe(gulpNgHtml2js, {
      moduleName: bower.name + '-templates',
      prefix: '/' + bower.name + '/',
      stripPrefix: '/src/app'
    })
    .pipe(gulpConcat, bower.name + '-templates.js')
    .pipe(gulp.dest, './target/client/tmp/app/js')()
    
gulp.task 'build-all', ['styles', 'bootstrap', 'templates', 'coffee', 'vendors', 'assets'], index

templateFiles = (opt) ->
  gulp.src(['./src/main/client/**/*.html', '!./src/main/client/index.html'], opt)
    .pipe(if opt and opt.min then gulpHtmlmin(htmlminOpts) else gulpUtil.noop())
    
appVendorFiles = () ->
  gulp.src('./target/client/tmp/vendors/**/*.js')

cssVendorFiles = () ->
  gulp.src('./target/client/tmp/vendors/css/**/*.css')

appFiles = () ->
  files = [
    './target/client/tmp/app/' + bower.name + '-templates.js',
    './target/client/tmp/app/**/*.js',
    '!./target/client/tmp/**/*_test.js'
  ]
  gulp.src(files)
    .pipe sort (a, b) -> compareStrings(a.relative, b.relative)
    .pipe gulpAngularFilesort()

cssFiles = (opt) ->
  gulp.src('./target/client/tmp/app/css/**/*.css', opt)
    .pipe(gulpOrder([
      'target/client/tmp/app/css/bootstrap.css',
      'target/client/tmp/app/css/app.css'
    ], {base: '.'}))
