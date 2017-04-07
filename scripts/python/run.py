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
result_groups = {
    'all': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC'
            '--length', '3',
            '--single-graph',
            '--operations', 'concat', 'delete', 'replace-char', 'reverse'
            'contains', 'equals',
            '--graph-file', 'all'
        ],
        'solve': [
            '--graph-files', 'all*.json',
            '--length', '3',
            '--concrete-solver',
            '--unbounded-solver',
            '--bounded-solver',
            '--aggregate-solver',
            '--weighted-solver',
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'all*',
            '--mc-reporter'
        ]
    },
    'concat': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC'
            '--length', '3',
            '--single-graph',
            '--operations', 'concat', 'contains', 'equals',
            '--graph-file', 'concat'
        ],
        'solve': [
            '--graph-files', 'concat*.json',
            '--length', '3',
            '--concrete-solver',
            '--unbounded-solver',
            '--bounded-solver',
            '--aggregate-solver',
            '--weighted-solver',
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'concat*',
            '--mc-reporter'
        ]
    },
    'delete': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC'
            '--length', '3',
            '--single-graph',
            '--operations', 'delete', 'contains', 'equals',
            '--graph-file', 'delete'
        ],
        'solve': [
            '--graph-files', 'delete*.json',
            '--length', '3',
            '--concrete-solver',
            '--unbounded-solver',
            '--bounded-solver',
            '--aggregate-solver',
            '--weighted-solver',
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'delete*',
            '--mc-reporter'
        ]
    },
    'replace': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC'
            '--length', '3',
            '--single-graph',
            '--operations', 'replace-char', 'contains', 'equals',
            '--graph-file', 'replace'
        ],
        'solve': [
            '--graph-files', 'replace*.json',
            '--length', '3',
            '--concrete-solver',
            '--unbounded-solver',
            '--bounded-solver',
            '--aggregate-solver',
            '--weighted-solver',
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'replace*',
            '--mc-reporter'
        ]
    },
    'reverse': {
        'generate': [
            '--ops-depth', '1',
            '--no-duplicates',
            '--unknown-string',
            '--non-uniform',
            '--inputs', 'ABC'
            '--length', '3',
            '--single-graph',
            '--operations', 'reverse', 'contains', 'equals',
            '--graph-file', 'reverse'
        ],
        'solve': [
            '--graph-files', 'reverse*.json',
            '--length', '3',
            '--concrete-solver',
            '--unbounded-solver',
            '--bounded-solver',
            '--aggregate-solver',
            '--weighted-solver',
            '--mc-reporter'
        ],
        'gather': [
            '--result-files', 'reverse*',
            '--mc-reporter'
        ]
    }
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


def main(arguments):
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
                            help='List of result groups to gather results for.')

    options = run_parser.parse_args(arguments)

    # check debug flag
    if options.debug:
        log.setLevel(logging.DEBUG)
        ch.setLevel(logging.DEBUG)
        log.debug('Args: {0}'.format(options))

    # compile sources
    compile_sources()

    # run generate graph script
    log.debug('Running Scripts: generate_graphs.py')
    for group in result_groups.keys():
        if not options.groups or group in options.groups:
            args = result_groups[group]['generate']
            if options.debug:
                args.append('--debug')
            log.debug('args: %s', ' '.join(args))
            generate_graphs.main(args)

    # run solvers via script
    log.debug('Running Scripts: run_solvers_on_graphs.py')
    for group in result_groups.keys():
        if not options.groups or group in options.groups:
            args = result_groups[group]['solve']
            if options.debug:
                args.append('--debug')
            log.debug('args: %s', ' '.join(args))
            run_solvers_on_graphs.main(args)

    # run analyze results script
    log.debug('Running Scripts: analyze_results.py')
    for group in result_groups.keys():
        if not options.groups or group in options.groups:
            args = result_groups[group]['gather']
            if options.debug:
                args.append('--debug')
            log.debug('args: %s', ' '.join(args))
            gather_results.main(args)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
