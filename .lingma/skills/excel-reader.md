---
name: excel-reader
description: 读取和解析 Excel 文件（.xlsx、.xls 格式），支持提取工作表数据、列信息、行数统计等。当需要读取 Excel 文件内容时使用此技能。
---

# Excel File Reader Skill

## 功能说明

此技能用于读取和解析 Excel 电子表格文件，支持以下功能：

1. **文件格式支持**
   - ✅ .xlsx (Excel 2007+)
   - ✅ .xls (Excel 97-2003)
   - ✅ .csv (逗号分隔值)

2. **核心能力**
   - 读取指定工作表或所有工作表
   - 提取行数据和列名
   - 统计行数、列数
   - 支持指定行范围读取
   - 自动识别数据类型（数字、文本、日期等）

3. **使用场景**
   - 分析 Excel 文件结构和内容
   - 提取特定列或行的数据
   - 数据迁移前的格式检查
   - 批量处理多个 Excel 文件

## 使用方法

### 基本用法
```bash
# 读取整个 Excel 文件
请读取文件：/path/to/file.xlsx

# 读取指定工作表
请读取 sales.xlsx 的 'Sheet1' 工作表

# 提取特定列
请从 data.xlsx 中提取 '姓名'、'年龄'、'部门' 列
```

### 高级用法
```bash
# 只读取前 N 行
读取 report.xlsx 的前 10 行数据

# 统计信息
统计 employees.xlsx 中有多少行数据，包含哪些列

# 多工作表处理
列出 inventory.xlsx 中的所有工作表名称
```

## 技术实现

### Python 实现方案
```python
import pandas as pd
import openpyxl
from pathlib import Path

def read_excel_file(file_path: str, sheet_name: str = None):
    """
    读取 Excel 文件
    
    Args:
        file_path: Excel 文件路径
        sheet_name: 工作表名称，默认为 None（读取第一个工作表）
    
    Returns:
        dict: 包含文件信息、工作表列表、数据预览等
    """
    file = Path(file_path)
    if not file.exists():
        raise FileNotFoundError(f"文件不存在：{file_path}")
    
    # 读取 Excel 文件
    excel_file = pd.ExcelFile(file_path)
    
    # 获取所有工作表名称
    sheet_names = excel_file.sheet_names
    
    # 如果没有指定工作表，默认读取第一个
    if sheet_name is None:
        sheet_name = sheet_names[0]
    
    # 读取数据
    df = pd.read_excel(file_path, sheet_name=sheet_name)
    
    return {
        'file_name': file.name,
        'file_path': str(file.absolute()),
        'sheet_names': sheet_names,
        'current_sheet': sheet_name,
        'row_count': len(df),
        'column_count': len(df.columns),
        'columns': list(df.columns),
        'data_preview': df.head(10).to_dict('records'),
        'dtypes': {col: str(dtype) for col, dtype in df.dtypes.items()}
    }

# 使用示例
if __name__ == '__main__':
    result = read_excel_file('test.xlsx')
    print(f"文件：{result['file_name']}")
    print(f"工作表：{result['sheet_names']}")
    print(f"行数：{result['row_count']}")
    print(f"列名：{result['columns']}")
    print(f"前 5 行数据：{result['data_preview'][:5]}")
```

### Node.js 实现方案
```javascript
const XLSX = require('xlsx');
const fs = require('fs');
const path = require('path');

function readExcelFile(filePath, sheetName = null) {
    // 检查文件是否存在
    if (!fs.existsSync(filePath)) {
        throw new Error(`文件不存在：${filePath}`);
    }
    
    // 读取 Excel 文件
    const workbook = XLSX.readFile(filePath);
    
    // 获取所有工作表名称
    const sheetNames = workbook.SheetNames;
    
    // 如果没有指定工作表，默认读取第一个
    if (sheetName === null) {
        sheetName = sheetNames[0];
    }
    
    // 获取工作表
    const worksheet = workbook.Sheets[sheetName];
    
    // 转换为 JSON
    const jsonData = XLSX.utils.sheet_to_json(worksheet);
    
    // 获取列名
    const columns = Object.keys(jsonData[0] || {});
    
    return {
        fileName: path.basename(filePath),
        filePath: path.resolve(filePath),
        sheetNames: sheetNames,
        currentSheet: sheetName,
        rowCount: jsonData.length,
        columnCount: columns.length,
        columns: columns,
        dataPreview: jsonData.slice(0, 10),
        sampleRow: jsonData[0] || {}
    };
}

// 使用示例
const result = readExcelFile('./test.xlsx');
console.log(`文件：${result.fileName}`);
console.log(`工作表：${result.sheetNames}`);
console.log(`行数：${result.rowCount}`);
console.log(`列名：${result.columns}`);
console.log(`前 5 行数据：`, result.dataPreview.slice(0, 5));
```

## 输出格式

成功响应示例：
```json
{
  "success": true,
  "data": {
    "fileName": "sales_report.xlsx",
    "filePath": "/path/to/sales_report.xlsx",
    "sheetNames": ["2024 Q1", "2024 Q2", "Summary"],
    "currentSheet": "2024 Q1",
    "rowCount": 156,
    "columnCount": 8,
    "columns": ["日期", "产品 ID", "产品名称", "单价", "数量", "金额", "地区", "销售员"],
    "dataPreview": [
      {
        "日期": "2024-01-01",
        "产品 ID": "P001",
        "产品名称": "笔记本电脑",
        "单价": 5999,
        "数量": 2,
        "金额": 11998,
        "地区": "北京",
        "销售员": "张三"
      }
      // ... 更多数据
    ],
    "dtypes": {
      "日期": "datetime64[ns]",
      "产品 ID": "object",
      "单价": "int64",
      "数量": "int64",
      "金额": "int64"
    }
  }
}
```

错误响应示例：
```json
{
  "success": false,
  "error": "文件不存在：/path/to/missing.xlsx",
  "availableFiles": [
    "/path/to/existing1.xlsx",
    "/path/to/existing2.xlsx"
  ]
}
```

## 注意事项

1. **大文件处理**
   - 对于超过 10000 行的文件，建议只读取前 N 行或使用流式读取
   - 可以添加 `max_rows` 参数限制读取行数

2. **编码问题**
   - .xlsx 文件通常没有编码问题
   - .csv 文件可能需要指定编码（utf-8, gbk, gb2312 等）

3. **性能优化**
   - 使用 `pandas` 的 `chunksize` 参数分块读取大文件
   - 使用 `openpyxl` 的 `read_only=True` 模式减少内存占用

4. **数据安全**
   - 不要读取包含敏感信息的 Excel 文件
   - 注意文件路径遍历攻击

## 依赖安装

### Python
```bash
pip install pandas openpyxl xlrd
```

### Node.js
```bash
npm install xlsx
```

## 常见问题

**Q: 如何处理包含中文的 Excel 文件？**
A: pandas 和 openpyxl 都能很好地处理中文，无需特殊配置。

**Q: 如何读取加密的 Excel 文件？**
A: 需要提供密码，可以使用 `msoffcrypto` 库先解密。

**Q: 如何保留 Excel 中的公式？**
A: openpyxl 支持读取和写入公式，设置 `data_only=False` 即可。

**Q: 如何处理超大 Excel 文件？**
A: 使用 pandas 的 `chunksize` 参数或 openpyxl 的 `read_only=True` 模式。
