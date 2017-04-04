#! /usr/bin/env python
import argparse
import logging

import logging
import os
import platform
import subprocess
import sys

import analyze_results
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


def compile_sources():
    # check if maven available
    check_cmd_cmd = 'where' if platform.system() == 'Windows' else 'hash'
    try:
        with open(os.devnull, 'w') as dev_null:
            subprocess.check_call([check_cmd_cmd, 'mvn'],
                                  stderr=dev_null,
                                  stdout=dev_null)

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
    generate_script_args = [
        ['--ops-depth', '1', '--no-duplicates', '--unknown-string', '--length', '3', '--single-graph', '--non-uniform', '--operations', 'concat', 'contains', 'equals', '--graph-file', 'concat'],
        ['--ops-depth', '1', '--no-duplicates', '--unknown-string', '--length', '3', '--single-graph', '--operations', 'delete', 'contains', 'equals', '--graph-file', 'delete'],
        ['--ops-depth', '1', '--no-duplicates', '--unknown-string', '--length', '3', '--single-graph', '--operations', 'replace-char', 'contains', 'equals', '--graph-file', 'replace']
    ]

    if options.debug:
        for args in generate_script_args:
            args.append('--debug')
    for args in generate_script_args:
        generate_graphs.main(args)

    # run solvers via script
    log.debug('Running Scripts: run_solvers_on_graphs.py')
    solver_script_args = [
        ['--graph-files', 'concat*.json', '--length', '3', '--concrete-solver', '--unbounded-solver', '--bounded-solver', '--aggregate-solver', '--weighted-solver', '--mc-reporter'],
        ['--graph-files', 'delete*.json', '--length', '3', '--concrete-solver', '--unbounded-solver', '--bounded-solver', '--aggregate-solver', '--weighted-solver', '--mc-reporter'],
        ['--graph-files', 'replace*.json', '--length', '3', '--concrete-solver', '--unbounded-solver', '--bounded-solver', '--aggregate-solver', '--weighted-solver', '--mc-reporter']
    ]

    if options.debug:
        for args in solver_script_args:
            args.append('--debug')
    for args in solver_script_args:
        run_solvers_on_graphs.main(args)

    # run analyze results script
    log.debug('Running Scripts: analyze_results.py')
    analyze_script_args = [
        ['--result-files', 'concat*', '--mc-reporter'],
        ['--result-files', 'delete*', '--mc-reporter'],
        ['--result-files', 'replace*', '--mc-reporter']
    ]

    if options.debug:
        for args in analyze_script_args:
            args.append('--debug')
    for args in analyze_script_args:
        analyze_results.main(args)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
