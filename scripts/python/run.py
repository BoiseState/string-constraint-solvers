#! /usr/bin/env python
import argparse
import logging
import platform
import subprocess
import sys

import os

import gather_results
import generate_graphs
import run_solvers_on_graphs

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

# globals
RESULT_GROUPS = {
    'all': {
        'generate': [
            '--ops-depth', '2',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--single-graph',
            '--operations', 'concat', 'delete', 'replace-char', 'contains',
            'equals'
        ],
        'solve': [
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'all*',
            '--single-out-file'
        ]
    },
    'concat': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC',
            '--single-graph',
            '--operations', 'concat', 'contains', 'equals'
        ],
        'solve': [
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'concat*',
            '--single-out-file'
        ]
    },
    'delete': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC',
            '--single-graph',
            '--operations', 'delete', 'contains', 'equals'
        ],
        'solve': [
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'delete*',
            '--single-out-file'
        ]
    },
    'replace': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC',
            '--single-graph',
            '--operations', 'replace-char', 'contains', 'equals'
        ],
        'solve': [
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'replace*',
            '--single-out-file'
        ]
    },
    'reverse': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC',
            '--single-graph',
            '--operations', 'reverse', 'contains', 'equals'
        ],
        'solve': [
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'reverse*',
            '--single-out-file'
        ]
    }
}

SOLVERS = {
    'concrete': '--concrete-solver',
    'unbounded': '--unbounded-solver',
    'bounded': '--bounded-solver',
    'aggregate': '--aggregate-solver',
    'weighted': '--weighted-solver'
}


def compile_sources():
    # check if maven available
    check_cmd_cmd = 'where' if platform.system() == 'Windows' else 'hash'
    try:
        with open(os.devnull, 'w') as dev_null:
            subprocess.check_call([check_cmd_cmd, 'mvn'],
                                  stderr=dev_null,
                                  stdout=dev_null,
                                  shell=platform.system() == 'Windows')

        maven_available = True
        log.debug('Maven available on the current system.')
    except subprocess.CalledProcessError as e:
        maven_available = False
        log.debug('CalledProcessError while determining if maven present on '
                  'system.')

    if maven_available:

        # any exceptions mean maven usage is not viable
        try:
            # use subprocess to compile classes with maven
            subprocess.call(['mvn', 'compile'], cwd=project_dir)

        except OSError as os_e:
            log.debug('OSError while getting class path from maven.')
        except ValueError as v_e:
            log.debug('ValueError while getting class path from maven.')
        except subprocess.CalledProcessError as cp_e:
            log.debug('CalledProcessError while getting class path from maven.')


def get_options(arguments):
    # process command line args
    run_parser = argparse.ArgumentParser(prog=__doc__,
                                         description='Run all full suite of'
                                                     ' scripts')
    run_parser.add_argument('-d',
                            '--debug',
                            help="Display debug messages for script.",
                            action="store_true")

    run_parser.add_argument('-g',
                            '--groups',
                            nargs='+',
                            default=list(),
                            help='List of result groups to gather results: '
                                 'all, concat, delete, replace, reverse')

    run_parser.add_argument('-l',
                            '--min-length',
                            default=1,
                            type=int,
                            help='The minimum length of strings for '
                                 'generated graphs.')

    run_parser.add_argument('-m',
                            '--max-length',
                            default=6,
                            type=int,
                            help='The maximum length of strings for '
                                 'generated graphs.')

    run_parser.add_argument('-r',
                            '--solvers',
                            nargs='+',
                            default=list(),
                            help='List of solvers to run: concrete, unbounded,'
                                 ' bounded, aggregate, weighted')

    run_parser.add_argument('-s',
                            '--steps',
                            nargs='+',
                            default=list(),
                            help='List of steps to run: generate, solve, '
                                 'gather')
    run_parser.add_argument('-t',
                            '--test-run',
                            help="Test run script.",
                            action="store_true")

    return run_parser.parse_args(arguments)


def main(arguments):
    options = get_options(arguments)

    # check debug flag
    if options.debug:
        log.setLevel(logging.DEBUG)
        ch.setLevel(logging.DEBUG)
        log.debug('Args: {0}'.format(options))

    # compile sources
    compile_sources()

    # run generate graph script
    if not options.steps or 'generate' in options.steps:
        log.debug('Running Scripts: generate_graphs.py')
        for group in sorted(RESULT_GROUPS.keys()):
            if not options.groups or group in options.groups:
                for i in range(options.min_length, options.max_length + 1):
                    args = list(RESULT_GROUPS[group]['generate'])
                    args.append('--length')
                    args.append(str(i))
                    args.append('--graph-file')
                    args.append('{0}{1:02d}'.format(group, i))
                    if options.debug:
                        args.append('--debug')
                    log.debug('%s args: %s', group, ' '.join(args))
                    if not options.test_run:
                        generate_graphs.main(args)

    # run solvers via script
    if not options.steps or 'solve' in options.steps:
        log.debug('Running Scripts: run_solvers_on_graphs.py')
        for group in sorted(RESULT_GROUPS.keys()):
            if not options.groups or group in options.groups:
                for i in range(options.min_length, options.max_length + 1):
                    args = list(RESULT_GROUPS[group]['solve'])
                    for solver in SOLVERS.keys():
                        if not options.solvers or solver in options.solvers:
                            args.append(SOLVERS.get(solver))
                    args.append('--length')
                    args.append(str(i))
                    args.append('--graph-files')
                    args.append('{0}{1:02d}*.json'.format(group, i))
                    if options.debug:
                        args.append('--debug')
                    log.debug('%s args: %s', group, ' '.join(args))
                    if not options.test_run:
                        run_solvers_on_graphs.main(args)

    # run analyze results script
    if not options.steps or 'gather' in options.steps:
        log.debug('Running Scripts: analyze_results.py')
        for group in sorted(RESULT_GROUPS.keys()):
            if not options.groups or group in options.groups:
                args = RESULT_GROUPS[group]['gather']
                if options.debug:
                    args.append('--debug')
                log.debug('%s args: %s', group, ' '.join(args))
                if not options.test_run:
                    gather_results.main(args)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
