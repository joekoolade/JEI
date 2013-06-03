//===------ JavaAOTCompiler.h - The J3 ahead of time compiler -------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef J3_AOT_COMPILER_H
#define J3_AOT_COMPILER_H

// for native_class_iterator
#include <map>

//#include "MvmDenseMap.h"
//#include "MvmDenseSet.h"
#include "j3/JavaLLVMCompiler.h"
#include "UTF8.h"
//#include "JMap.h"

// for stderr
#include <cstdio>

namespace j3 {

class ArrayObject;
class Attribut;
class ClassBytes;
class JavaCompiler;
class ZipArchive;
class Class;
class ClassArray;
class ClassBytes;
class ClassMap;
class Classpath;
class ClassLoader;
class CommonClass;

class JavaAOTCompiler : public JavaLLVMCompiler {

public:
  JavaAOTCompiler(const std::string &ModuleID);
  
  virtual bool isStaticCompiling() {
    return true;
  }
  
  virtual bool emitFunctionName() {
    return true;
  }
  
  virtual JavaCompiler* Create(const std::string& ModuleID) {
    return new JavaAOTCompiler(ModuleID);
  }
  
  virtual void* materializeFunction(JavaMethod* meth, Class* customizeFor) {
    fprintf(stderr, "Can not material a function in AOT mode.");
    abort();
  }

  virtual void* GenerateStub(llvm::Function* F) {
    // Do nothing in case of AOT.
    return NULL;
  }
  
  virtual llvm::Function* getMethod(JavaMethod* meth, Class* customizeFor);
  
  virtual void makeVT(Class* cl);
  virtual void makeIMT(Class* cl);
 
  llvm::Constant* HandleMagic(JavaObject* obj, CommonClass* cl);
  virtual llvm::Constant* getFinalObject(JavaObject* obj, CommonClass* cl);
  virtual JavaObject* getFinalObject(llvm::Value* C);
  virtual llvm::GlobalVariable* getNativeClass(CommonClass* cl);
  virtual llvm::Constant* getClassBytes(const UTF8* name, ClassBytes* bytes);
  virtual llvm::Constant* getJavaClass(CommonClass* cl);
  virtual llvm::Constant* getJavaClassPtr(CommonClass* cl);
  virtual llvm::Constant* getStaticInstance(Class* cl);
  virtual llvm::Constant* getVirtualTable(JavaVirtualTable*);
  virtual llvm::Constant* getMethodInClass(JavaMethod* meth);
  
  virtual llvm::Constant* getString(JavaString* str);
  virtual llvm::Constant* getStringPtr(JavaString** str);
  virtual llvm::Constant* getResolvedConstantPool(JavaConstantPool* ctp);
  virtual llvm::Constant* getNativeFunction(JavaMethod* meth, void* natPtr);
  
  virtual void setMethod(llvm::Function* func, void* ptr, const char* name);
  

  virtual ~JavaAOTCompiler() {}
  
  virtual CommonClass* getUniqueBaseClass(CommonClass* cl);

  void analyseClasspathEnv(const char* str);

private:

  //--------------- Static compiler specific functions -----------------------//
  llvm::Constant* CreateConstantFromVT(JavaVirtualTable* VT);
  llvm::Constant* CreateConstantFromUTF8(const UTF8* val);
  llvm::Constant* CreateConstantFromCommonClass(CommonClass* cl);
  llvm::Constant* CreateConstantFromClass(Class* cl);
  llvm::Constant* CreateConstantFromClassPrimitive(ClassPrimitive* cl);
  llvm::Constant* CreateConstantFromClassArray(ClassArray* cl);
  llvm::Constant* CreateConstantFromAttribut(Attribut& attribut);
  llvm::Constant* CreateConstantFromJavaField(JavaField& field);
  llvm::Constant* CreateConstantFromJavaMethod(JavaMethod& method);
  llvm::Constant* CreateConstantFromStaticInstance(Class* cl);
  llvm::Constant* CreateConstantFromJavaString(JavaString* str);
  llvm::Constant* CreateConstantForBaseObject(CommonClass* cl);
  llvm::Constant* CreateConstantFromJavaObject(JavaObject* obj);
  llvm::Constant* CreateConstantFromClassBytes(ClassBytes* bytes);
  llvm::Constant* CreateConstantFromJavaConstantPool(JavaConstantPool* ctp);
  llvm::Constant* CreateConstantFromClassMap(const MvmDenseMap<const UTF8*, CommonClass*>& map);
  llvm::Constant* CreateConstantFromUTF8Map(const MvmDenseSet<UTF8MapKey, const UTF8*>& set);
  void AddInitializerToClass(llvm::GlobalVariable* varGV, CommonClass* classDef);
  llvm::Constant* getUTF8(const UTF8* val);
  
  template<typename T>
  llvm::Constant* CreateConstantFromIntArray(const T* val, llvm::Type* Ty);
  
  template<typename T>
  llvm::Constant* CreateConstantFromFPArray(const T* val, llvm::Type* Ty);

  llvm::Constant* CreateConstantFromObjectArray(const ArrayObject* val);

  // A map of classes and their global variable
  std::map<CommonClass*, llvm::GlobalVariable*> nativeClasses;
  std::map<ClassBytes*, llvm::GlobalVariable*> classBytes;
  std::map<const ClassArray*, llvm::GlobalVariable*> arrayClasses;
  std::map<const CommonClass*, llvm::Constant*> javaClasses;
  std::map<const JavaVirtualTable*, llvm::Constant*> virtualTables;
  std::map<const Class*, llvm::Constant*> staticInstances;
  std::map<const JavaConstantPool*, llvm::Constant*> resolvedConstantPools;
  std::map<const JavaString*, llvm::Constant*> strings;
  std::map<const JavaMethod*, llvm::Constant*> nativeFunctions;
  std::map<const UTF8*, llvm::Constant*> utf8s;
  std::map<const Class*, llvm::Constant*> virtualMethods;
  std::map<const JavaObject*, llvm::Constant*> finalObjects;
  std::map<const llvm::Constant*, JavaObject*> reverseFinalObjects;
  std::vector<std::pair<JavaMethod*, Class*> > toCompile;
  std::vector<Class*> classes;
  ClassLoader* loader;

  typedef std::map<const JavaObject*, llvm::Constant*>::iterator
    final_object_iterator;
  
  typedef std::map<const llvm::Constant*, JavaObject*>::iterator
    reverse_final_object_iterator;

  typedef std::map<const Class*, llvm::Constant*>::iterator
    method_iterator;
  
  typedef std::map<CommonClass*, llvm::GlobalVariable*>::iterator
    native_class_iterator; 
  
  typedef std::map<ClassBytes*, llvm::GlobalVariable*>::iterator
    class_bytes_iterator; 
  
  typedef std::map<const ClassArray*, llvm::GlobalVariable*>::iterator
    array_class_iterator;
  
  typedef std::map<const CommonClass*, llvm::Constant*>::iterator
    java_class_iterator;
  
  typedef std::map<const JavaVirtualTable*, llvm::Constant*>::iterator
    virtual_table_iterator;
  
  typedef std::map<const Class*, llvm::Constant*>::iterator
    static_instance_iterator;
  
  typedef std::map<const JavaConstantPool*, llvm::Constant*>::iterator
    resolved_constant_pool_iterator;

  typedef std::map<const JavaString*, llvm::Constant*>::iterator
    string_iterator;
  
  typedef std::map<const JavaMethod*, llvm::Constant*>::iterator
    native_function_iterator;
  
  typedef std::map<const UTF8*, llvm::Constant*>::iterator
    utf8_iterator;

  bool isCompiling(const CommonClass* cl) const;
  Class* loadClass;

public:
  llvm::Function* StaticInitializer;
  llvm::Function* ObjectPrinter;
  llvm::Function* Callback;
  llvm::Function* ArrayObjectTracer;
  llvm::Function* RegularObjectTracer;
  llvm::Function* JavaObjectTracer;
  llvm::Function* EmptyDestructorFunction;
  llvm::Function* ReferenceObjectTracer;
  llvm::GlobalVariable* UTF8TombstoneGV;
  llvm::GlobalVariable* UTF8EmptyGV;
  
  bool generateStubs;
  bool assumeCompiled;
  bool compileRT;
  bool precompile;
  bool emitClassBytes;
  const char* bootClasspathEnv;
  std::vector<std::string>* clinits;
  /// bootClasspath - List of paths for the base classes.
  ///
  std::vector<const char*> bootClasspath;

  /// bootArchives - List of .zip or .jar files that contain base classes.
  ///
  std::vector<ZipArchive*> bootArchives;

  /// dirSeparator - Directory separator for file paths, e.g. '\' for windows,
  /// '/' for Unix.
  ///
  static const char* dirSeparator;

  /// envSeparator - Paths separator for environment variables, e.g. ':'.
  ///
  static const char* envSeparator;

  /// Magic - The magic number at the beginning of each .class file. 0xcafebabe.
  ///
  static const unsigned int Magic;

  
  
  void CreateStaticInitializer();
  
  void printStats();
  
  void compileFile(const char* name);
  void compileClass(Class* cl);
  void compileClassLoader();
  void generateClassBytes(ClassLoader *);
  void generateMain(const char* name, bool jit);
  const char *getHostTriple();
  void mainCompilerStart();
private:
  void extractBootClasses();
  void extractFiles(ClassBytes*, std::vector<std::string>);
  void extractFiles(ClassBytes* bytes, ClassLoader *loader);
  void compileAllStubs(Signdef* sign);
  Class* internalLoad(const UTF8* name, ClassBytes* data);
  CommonClass* lookupClass(const UTF8* utf8);
  llvm::Function* getMethodOrStub(JavaMethod* meth, Class* customizeFor);
};

} // end namespace j3

#endif