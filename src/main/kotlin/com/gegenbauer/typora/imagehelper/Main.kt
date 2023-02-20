package com.gegenbauer.typora.imagehelper

fun main(args: Array<String>) {
    runCatching {
        if (args.first() == "-h") {
            MdMigrationHelper.printHelpMessage()
            return
        }

        MdMigrationHelper.typoraRootDir = args[0]
        if (!isDirValid(MdMigrationHelper.typoraRootDir)) {
            return
        }

        MigrationRule.rule = MigrationRule.parseRule(args[1].toInt())
        if (MigrationRule.rule == MigrationRule.RuleSpecific) {
            IMigration.specificImageDir = args[2]
        }

        checkAndCreateDir(IMigration.specificImageDir)

        if (IMigration.specificImageDir.isNotEmpty() && !isDirValid(IMigration.specificImageDir)) {
            return
        }
        println("rule: ${MigrationRule.rule.javaClass.simpleName}, targetPath: ${IMigration.specificImageDir}")

        MdMigrationHelper.startMigration()
    }.onFailure {
        when (it) {
            is ArrayIndexOutOfBoundsException -> println("invalid args!")
            else -> println("unknown error: ${it.message}")
        }
    }
}

