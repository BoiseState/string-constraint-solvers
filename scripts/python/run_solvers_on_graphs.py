#! /usr/bin/env python

import argparse
import fnmatch
import logging
import os
import platform
import subprocess
import sys

# set relevent path and file variables
file_name = os.path.basename(__file__).replace('.py', '')
project_dir = '{0}/../..'.format(os.path.dirname(__file__))
project_dir = os.path.normpath(project_dir)

# configure logging
log = logging.getLogger(file_name)
log.setLevel(logging.ERROR)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.ERROR)

formatter = logging.Formatter(
    u'[%(name)s:%(levelname)s] %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)


class Solver:
    def __init__(self, name, args):
        self.name = name
        self.args = args


class Reporter:
    def __init__(self, name, arg):
        self.name = name
        self.arg = arg


class Settings:
    def __init__(self, options):
        # iniitialize framework args
        self.framework_args = options.framework_args

        # set debug
        self.debug = options.debug
        if self.debug:
            log.setLevel(logging.DEBUG)
            ch.setLevel(logging.DEBUG)
            log.debug('Args: %s', options)

        # set graph file pattern
        self.graph_file_pattern = options.graph_files
        log.debug('graph file pattern: "%s"', self.graph_file_pattern)

        # initilize framework length argument
        self.length = options.length
        log.debug('length: %s', self.length)

        # initialize solver set with framework arguments for each solver
        self.solvers = set()
        if options.unbounded_solver:
            self.solvers.add(Solver('unbounded', ['--solver',
                                                  'jsa',
                                                  '--model-version',
                                                  '1']))
        if options.bounded_solver:
            self.solvers.add(Solver('bounded', ['--solver',
                                                'jsa',
                                                '--model-version',
                                                '2']))
        if options.aggregate_solver:
            self.solvers.add(Solver('aggregate', ['--solver',
                                                  'jsa',
                                                  '--model-version',
                                                  '3']))
        if options.weighted_solver:
            self.solvers.add(Solver('weighted', ['--solver',
                                                 'jsa',
                                                 '--model-version',
                                                 '4']))
        if options.concrete_solver or len(self.solvers) == 0:
            self.solvers.add(Solver('concrete', ['--solver',
                                                 'concrete']))
        if self.debug:
            for s in self.solvers:
                log.debug('solver name: "%s"', s.name)
                log.debug('solver arg: "%s"', ' '.join(s.args))

        # initialize reporter framework arguments
        if options.mc_reporter:
            self.reporter = 'model-count'
        if options.sat_reporter:
            self.reporter = 'sat'
        log.debug('reporter: "%s"', self.reporter)

        # update framework args
        self.framework_args.insert(0, '--reporter')
        self.framework_args.insert(1, self.reporter)
        self.framework_args.insert(2, '--length')
        self.framework_args.insert(3, self.length)


def get_options(arguments):
    # process command line args
    solver_parser = argparse.ArgumentParser(prog=__doc__,
                                            description='Run the string '
                                                        'constraint '
                                                        'solver framework for '
                                                        'a '
                                                        'specified set of '
                                                        'solvers on '
                                                        'a specified set of '
                                                        'graphs '
                                                        'using a specified '
                                                        'reporter.')

    solver_parser.add_argument('-a',
                               '--framework-args',
                               nargs='*',
                               default=list(),
                               help='List of additional args to pass to the '
                                    'constraint solver framework.')

    solver_parser.add_argument('-d',
                               '--debug',
                               help='Display debug messages for both this '
                                    'script and'
                                    'the constraint solver framework.',
                               action="store_true")

    solver_parser.add_argument('-f',
                               '--graph-files',
                               default='*.json',
                               help='A Unix shell-style pattern which is used '
                                    'to '
                                    'match a set of constraint graph files '
                                    'which '
                                    'will be solved by the constraint solver '
                                    'framework.')

    solver_parser.add_argument('-l',
                               '--length',
                               default=2,
                               help='The initial bounding length suplied to '
                                    'the '
                                    'constraint solver framework.')

    # solver argument group
    solvers = solver_parser.add_argument_group('solvers',
                                               'String constraint solvers '
                                               'which can '
                                               'be used in the constraint '
                                               'solver '
                                               'framework. If no solver is '
                                               'specified, the default solver '
                                               'is the '
                                               'concrete solver.')

    solvers.add_argument('-c',
                         '--concrete-solver',
                         help='Include the concrete solver in the set of '
                              'solvers.',
                         action='store_true')

    solvers.add_argument('-u',
                         '--unbounded-solver',
                         help='Include the unbounded automaton model solver in '
                              'the set of solvers.',
                         action='store_true')

    solvers.add_argument('-b',
                         '--bounded-solver',
                         help='Include the bounded automaton model solver in '
                              'the set of solvers.',
                         action='store_true')

    solvers.add_argument('-g',
                         '--aggregate-solver',
                         help='Include the aggregate automata model solver in '
                              'the set of solvers.',
                         action='store_true')

    solvers.add_argument('-w',
                         '--weighted-solver',
                         help='Include the weighted transition automata model '
                              'solver in the set of solvers.',
                         action='store_true')

    # reporter argument group
    reporters = solver_parser.add_mutually_exclusive_group(required=True)

    reporters.add_argument('-m',
                           '--mc-reporter',
                           help='Informs the constraint solver framework to '
                                'utilize the model count reporter when solving '
                                'constraint graphs.',
                           action='store_true')

    reporters.add_argument('-s',
                           '--sat-reporter',
                           help='Informs the constraint solver framework to '
                                'utilize the sat reporter when solving '
                                'constraint graphs.',
                           action='store_true')

    return Settings(solver_parser.parse_args(arguments))


def get_graph_files(settings):
    # initialize set for graph file paths
    graph_files = set()

    # get graph directory path
    graph_dir_path = os.path.join(project_dir, 'graphs')

    # for all matching files in graph directory
    for f in os.listdir(graph_dir_path):
        if os.path.isfile(os.path.join(project_dir, 'graphs', f)) and \
                fnmatch.fnmatch(f, settings.graph_file_pattern):
            # add file to graph files set
            graph_files.add(os.path.join(graph_dir_path, f))

    # return graph file set
    if settings.debug:
        for f in sorted(graph_files):
            log.debug('included graph file: %s', f)
    return sorted(graph_files)


def get_classpath():
    # initize class path
    class_path = ''

    # check if maven available
    check_cmd_cmd = 'where' if platform.system() == 'Windows' else 'hash'
    try:
        with open(os.devnull, 'w') as dev_null:
            subprocess.check_call([check_cmd_cmd, 'mvn'],
                                  stderr=dev_null,
                                  stdout=dev_null,
                                  cwd=project_dir)

        maven_available = True
        log.debug('Maven available on the current system.')
    except subprocess.CalledProcessError as e:
        maven_available = False
        log.debug('CalledProcessError while determining if maven present on '
                  'system.')

    if maven_available:

        # any exceptions mean maven usage is not viable
        try:
            # add target directories for maven build configuration
            class_path += os.path.join(project_dir, 'target', 'classes')

            # ensure mvn dependency plugin installed
            with open(os.devnull, 'w') as dev_null:
                cmd = ['mvn', 'help:describe', '-Dplugin=dependency']
                subprocess.check_call(cmd,
                                      stderr=dev_null,
                                      stdout=dev_null,
                                      cwd=project_dir)

            # use subprocess to get class path from maven
            cmd = ['mvn', 'dependency:build-classpath']
            sp1 = subprocess.Popen(cmd,
                                   stdout=subprocess.PIPE,
                                   cwd=project_dir)
            cmd = ['grep', '-v', '^\[INFO\]']
            sp2 = subprocess.Popen(cmd,
                                   stdin=sp1.stdout,
                                   stdout=subprocess.PIPE,
                                   cwd=project_dir)
            sp1.stdout.close()
            mvn_class_path = sp2.communicate()[0].strip()

            # return current classpath concatenated with maven classpath
            log.debug('Maven build classpath: "%s"', mvn_class_path)
            return '{0}:{1}'.format(class_path, mvn_class_path)

        except OSError as os_e:
            log.debug('OSError while getting class path from maven.')
        except ValueError as v_e:
            log.debug('ValueError while getting class path from maven.')
        except subprocess.CalledProcessError as cp_e:
            log.debug('CalledProcessError while getting class path from maven.')

    # maven not available or unsuccessful
    class_path += os.path.join(project_dir, 'bin')

    # for all matching files in lib directory
    for f in os.listdir(os.path.join(project_dir, 'lib')):
        if os.path.isfile(f) and \
                fnmatch.fnmatch(f, '*.jar'):
            # add file path to class path
            class_path += os.path.join(project_dir, 'lib', f)

    # return classpath
    log.debug('Constructed classpath: %s', class_path)
    return class_path


def run_solver(solver, files, class_path, settings):
    # ensure results directory exists
    results_dir = os.path.join(project_dir,
                               'results',
                               settings.reporter,
                               solver.name)
    if not os.path.exists(results_dir):
        log.debug('creating results dir: %s', results_dir)
        os.makedirs(results_dir)

    # clean up result directory
    result_file_pattern = settings.graph_file_pattern
    if len(os.path.splitext(result_file_pattern)) > 1:
        result_file_pattern = result_file_pattern.replace('json', 'csv')
    for f in os.listdir(results_dir):
        file_path = os.path.join(results_dir, f)
        if os.path.isfile(file_path) and \
                fnmatch.fnmatch(f, result_file_pattern):
            os.remove(file_path)

    # for each graph file
    for gf in files:
        # set results file
        gf_base = os.path.basename(gf)
        result_filename = '{0}.csv'.format(os.path.splitext(gf_base)[0])
        result_filepath = os.path.join(results_dir, result_filename)
        log.debug('result file: %s', result_filepath)

        # invoke java to run solver framework on file
        cmd = ['java',
               '-cp',
               class_path,
               '-Xmx2g',
               'edu.boisestate.cs.SolveMain',
               gf]
        # include solver args
        for arg in solver.args:
            cmd.append(arg)
        # include other framework args
        for arg in settings.framework_args:
            cmd.append(arg)
        log.debug('Command: %s', ' '.join(['java -cp <CLASSPATH> -Xmx2g '
                                           'edu.boisestate.cs.SolveMain '
                                           '{0}'.format(gf),
                                           ' '.join(solver.args),
                                           ' '.join(settings.framework_args)]))

        try:
            with open(result_filepath, 'w') as out_file:
                sp1 = subprocess.Popen(cmd,
                                       stderr=subprocess.STDOUT,
                                       stdout=out_file)
                sp1.wait()
        except OSError as os_e:
            log.debug('OSError while running solver framework.')
        except ValueError as v_e:
            log.debug('ValueError while running solver framework.')
        except subprocess.CalledProcessError as cp_e:
            log.debug('CalledProcessError while  running solver framework.')


def main(arguments):
    # get settings from arguments
    settings = get_options(arguments)

    # get graph files
    files = get_graph_files(settings)

    # get java classpath for framework
    class_path = get_classpath()

    # for each specified solver
    for solver in settings.solvers:
        # run solver
        run_solver(solver, files, class_path, settings)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
