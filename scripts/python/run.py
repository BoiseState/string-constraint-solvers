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

    args = run_parser.parse_args(arguments)

    # check debug flag
    if args.debug:
        log.setLevel(logging.DEBUG)
        ch.setLevel(logging.DEBUG)
        log.debug('Args: {0}'.format(args))

    # compile sources
    compile_sources()

    # run generate graph script
    log.debug('Running Script: generate_graphs.py')
    generate_script_args = ['--ops-depth',
                            '2',
                            '--no-duplicates',
                            '--unknown-string',
                            '--length',
                            '2',
                            '--single-graph']

    if args.debug:
        generate_script_args.append('--debug')
    generate_graphs.main(generate_script_args)

    # run solvers via script
    log.debug('Running Script: run_solvers_on_graphs.py')
    solver_script_args = ['--graph-files',
                          'gen*.json',
                          '--length',
                          '2',
                          '--concrete-solver',
                          '--unbounded-solver',
                          '--bounded-solver',
                          '--aggregate-solver',
                          '--mc-reporter']

    if args.debug:
        solver_script_args.append('--debug')
    run_solvers_on_graphs.main(solver_script_args)

    # run analyze results script
    log.debug('Running Script: analyze_results.py')
    analyze_script_args = ['--result-files', 'gen*', '--mc-reporter']

    if args.debug:
        analyze_script_args.append('--debug')
    analyze_results.main(analyze_script_args)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
