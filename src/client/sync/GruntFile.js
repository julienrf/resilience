module.exports = function (grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    coffee: {
      compile: {
        files: {
          'build/src-generated/<%= pkg.name %>.js': 'src/<%= pkg.name %>.coffee'
        }
      }
    },
    uglify: {
      build: {
        src: 'build/src-generated/<%= pkg.name %>.js',
        dest: 'build/<%= pkg.name %>.min.js'
      }
    }
  })
  
  grunt.loadNpmTasks('grunt-contrib-coffee');
  grunt.loadNpmTasks('grunt-contrib-uglify');

  grunt.registerTask('default', ['coffee', 'uglify']);
  
};
