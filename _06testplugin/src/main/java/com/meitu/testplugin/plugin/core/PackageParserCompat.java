package com.meitu.testplugin.plugin.core;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.Build;

import com.meitu.testplugin.plugin.util.Reflector;

import java.io.File;

public class PackageParserCompat {

    // TODO:这部对应的操作是什么？what，why，how? 2020-06-22
    public static final PackageParser.Package parsePackage(final Context context, final File apk, final int flags) {
        try {
            if (Build.VERSION.SDK_INT >= 28
                    || (Build.VERSION.SDK_INT == 27 && Build.VERSION.PREVIEW_SDK_INT != 0)) { // Android P Preview
                return PackageParserPPreview.parsePackage(context, apk, flags);
            } else if (Build.VERSION.SDK_INT >= 24) {
                return PackageParserV24.parsePackage(context, apk, flags);
            } else if (Build.VERSION.SDK_INT >= 21) {
                return PackageParserLollipop.parsePackage(context, apk, flags);
            } else {
                return PackageParserLegacy.parsePackage(context, apk, flags);
            }

        } catch (Throwable e) {
            throw new RuntimeException("error", e);
        }
    }

    private static final class PackageParserPPreview {
        static final PackageParser.Package parsePackage(Context context, File apk, int flags) throws Throwable {
            PackageParser parser = new PackageParser();
            PackageParser.Package pkg = parser.parsePackage(apk, flags);
            Reflector.with(parser)
                    .method("collectCertificates", PackageParser.Package.class, boolean.class)
                    .call(pkg, false);
            return pkg;
        }
    }

    private static final class PackageParserV24 {

        static final PackageParser.Package parsePackage(Context context, File apk, int flags) throws Throwable {
            PackageParser parser = new PackageParser();
            PackageParser.Package pkg = parser.parsePackage(apk, flags);
            Reflector.with(parser)
                    .method("collectCertificates", PackageParser.Package.class, int.class)
                    .call(pkg, flags);
            return pkg;
        }
    }

    private static final class PackageParserLollipop {

        static final PackageParser.Package parsePackage(final Context context, final File apk, final int flags) throws Throwable {
            PackageParser parser = new PackageParser();
            PackageParser.Package pkg = parser.parsePackage(apk, flags);
            parser.collectCertificates(pkg, flags);
            return pkg;
        }

    }

    private static final class PackageParserLegacy {

        static final PackageParser.Package parsePackage(Context context, File apk, int flags) throws Throwable {
            PackageParser parser = new PackageParser(apk.getAbsolutePath());
            PackageParser.Package pkg = parser.parsePackage(apk, apk.getAbsolutePath(), context.getResources().getDisplayMetrics(), flags);
            Reflector.with(parser)
                    .method("collectCertificates", PackageParser.Package.class, int.class)
                    .call(pkg, flags);
            return pkg;
        }

    }


}
