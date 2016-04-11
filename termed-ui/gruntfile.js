module.exports = function(grunt) {
  'use strict';

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-wiredep');
  grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-connect-proxy');

  grunt.registerTask('default', ['jshint', 'karma:unit', 'wiredep', 'copy']);
  grunt.registerTask('dev', ['configureProxies:server', 'connect', 'karma:watch']);

  grunt.initConfig({

    pkg: grunt.file.readJSON('package.json'),
    bower: grunt.file.readJSON('.bowerrc'),

    connect: {
      server: {
        options: {
          base: 'src',
          middleware: function (connect, options, defaultMiddleware) {
            var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest;
            return [proxy].concat(defaultMiddleware);
          }
        },
        proxies: [
          {
            context: '/api',
            host: 'localhost',
            port: '8080'
          }
        ]
      }
    },

    wiredep: {
      task: {
        src: ['src/index.html']
      }
    },

    copy: {
      dist: {
        files: [{
          expand: true,
          cwd: 'src',
          src: '**',
          dest: 'dist'
        }]
      }
    },

    jshint: {
      files: ['gruntfile.js', 'src/app/**/*.js']
    },

    karma: {
      options: {
        frameworks: ['jasmine'],
        browsers: ['PhantomJS'],
        files: [
          'src/lib/angular/angular.js',
          'src/lib/angular-mocks/angular-mocks.js',
          'test/**/*.js'
        ]
      },
      unit: {
        options: {
          singleRun: true,
          autoWatch: false
        }
      },
      watch: {
        options: {
          singleRun: false,
          autoWatch: true
        }
      }
    }

  });
};
