#!/usr/bin/env python
# -*- coding:utf-8 -*-
'''
地方棋牌活动页面
'''
import time
from runcenter.enums import EnumPriority,EnumStatus
from runcenter.testcase import debug_run_all,TestCase
from appiumcenter.luadriver import LuaDriver


	
class Test01(TestCase):
    '''
    recorder 
    '''
    owner = "recorder"
    status = EnumStatus.Design
    priority = EnumPriority.High
    timeout = 5
    def pre_test(self):
        self.luaobj = LuaDriver()
        self.luaobj.creatLuaDriver()
        self.luadriver = self.luaobj.getLuaDriver()

    def post_test(self):
        '''
        测试用例执行完成后，清理测试环境
        '''
        self.luaobj.closeLuadriver()
    def run_test(self):