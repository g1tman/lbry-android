
from pythonforandroid.toolchain import CythonRecipe, Recipe, shprint, current_directory, info
from pythonforandroid.patching import will_build, check_any
import sh
from os.path import join


class PyjniusRecipe(CythonRecipe):
    version = 'master'
    url = 'https://github.com/kivy/pyjnius/archive/{version}.zip'
    name = 'pyjnius'
    depends = [('python2', 'python3crystax'), ('sdl2', 'sdl', 'genericndkbuild'), 'six']
    site_packages_name = 'jnius'
    call_hostpython_via_targetpython = False

    patches = [('sdl2_jnienv_getter.patch', will_build('sdl2')),
               ('genericndkbuild_jnienv_getter.patch', will_build('genericndkbuild'))]

    def get_recipe_env(self, arch):
        env = super(PyjniusRecipe, self).get_recipe_env(arch)
        target_python = Recipe.get_recipe('python2', self.ctx).get_build_dir(arch.arch)
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python2.7'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython2.7'

        return env

    def postbuild_arch(self, arch):
        super(PyjniusRecipe, self).postbuild_arch(arch)
        info('Copying pyjnius java class to classes build dir')
        with current_directory(self.get_build_dir(arch.arch)):
            shprint(sh.cp, '-a', join('jnius', 'src', 'org'), self.ctx.javaclass_dir)


recipe = PyjniusRecipe()
